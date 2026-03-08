package com.dinethdf.ClinicManagementSystem.Service.Appointment;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Appointment.StaffAppointmentManageDTO;
import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import com.dinethdf.ClinicManagementSystem.Model.Appointment.AppointmentStatusLookup;
import com.dinethdf.ClinicManagementSystem.Repo.Appointment.AppointmentRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Appointment.AppointmentStatusLookupRepo;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AppointmentManageStaffService {

    private final AppointmentRepo appointmentRepo;
    private final AppointmentStatusLookupRepo statusLookupRepo;
    private final QueueService queueService;

    // 1. Set to BOOKED (Waiting Queue) -> Updates MySQL & Firebase
    @Transactional
    public ApiResponse<String> markAsBooked(StaffAppointmentManageDTO request) {
        Appointment appointment = validateAndGetAppointment(request.getAppointmentId(), request.getPatientId());
        return updateAppointmentState(appointment, "BOOKED", "WAITING");
    }

    // 2. Set to ONGOING (Call Patient) -> Updates FIREBASE ONLY
    @Transactional
    public ApiResponse<String> markAsOngoing(StaffAppointmentManageDTO request) {
        Appointment appointment = validateAndGetAppointment(request.getAppointmentId(), request.getPatientId());
        // Pass null for dbStatusCode to skip the MySQL update entirely
        return updateAppointmentState(appointment, null, "ONGOING");
    }

    // 3. Set to COMPLETED (Finished) -> Updates MySQL & Firebase
    @Transactional
    public ApiResponse<String> markAsCompleted(StaffAppointmentManageDTO request) {
        Appointment appointment = validateAndGetAppointment(request.getAppointmentId(), request.getPatientId());
        return updateAppointmentState(appointment, "COMPLETED", "COMPLETED");
    }

    // --- Core Logic (Updates MySQL & Firebase) ---
    private ApiResponse<String> updateAppointmentState(Appointment appointment, String dbStatusCode, String firebaseAction) {

        // 1. Update MySQL DB (ONLY if a dbStatusCode is provided)
        if (dbStatusCode != null) {
            // Prevent duplicate updates
            if (appointment.getStatus() != null && appointment.getStatus().getCode().equals(dbStatusCode)) {
                throw new ValidationException("Appointment is already " + dbStatusCode, "ALREADY_" + dbStatusCode, HttpStatus.BAD_REQUEST);
            }

            AppointmentStatusLookup newStatus = statusLookupRepo.findByCode(dbStatusCode)
                    .orElseThrow(() -> new ValidationException("Status '" + dbStatusCode + "' not found.", "CONFIG_ERROR", HttpStatus.INTERNAL_SERVER_ERROR));

            appointment.setStatus(newStatus);
            appointmentRepo.save(appointment);
        }

        // 2. Update Firebase Queue (Always happens)
        Long clinicId = Long.valueOf(TenantContext.getCurrentTenant());
        Long scheduleId = Long.valueOf(appointment.getDoctorSchedule().getId());
        int queueNo = appointment.getQueuePosition();

        switch (firebaseAction) {
            case "WAITING":
                queueService.againWaitPatient(clinicId, scheduleId, queueNo);
                break;
            case "ONGOING":
                queueService.callPatient(clinicId, scheduleId, queueNo);
                break;
            case "COMPLETED":
                queueService.completePatient(clinicId, scheduleId, queueNo);
                break;
        }

        String actionState = dbStatusCode != null ? dbStatusCode : firebaseAction;
        return ApiResponse.success("SUCCESS", "Appointment marked as " + actionState + ".");
    }

    // --- Helper Method for Security & Validation ---
    private Appointment validateAndGetAppointment(Long appointmentId, Long patientId) {
        Integer clinicId = TenantContext.getCurrentTenant();

        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ValidationException("Appointment not found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!Objects.equals(appointment.getClinicId(), clinicId)) {
            throw new ValidationException("Appointment not found", "NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        if (!Objects.equals(appointment.getPatient().getId(), patientId)) {
            throw new ValidationException("Patient mismatch", "PATIENT_MISMATCH", HttpStatus.BAD_REQUEST);
        }

        return appointment;
    }
}