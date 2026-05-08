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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentManageStaffService {

    private final AppointmentRepo appointmentRepo;
    private final AppointmentStatusLookupRepo statusLookupRepo;
    private final QueueService queueService;

    // 1. Set to BOOKED (Waiting Queue)
    @Transactional
    public ApiResponse<String> markAsBooked(StaffAppointmentManageDTO request) {
        Appointment appointment = validateAndGetAppointment(request.getAppointmentId(), request.getPatientId());

        if (isStatusAlready(appointment, "BOOKED")) {
            throw new ValidationException("Appointment is already BOOKED", "ALREADY_BOOKED", HttpStatus.BAD_REQUEST);
        }

        return updateAppointmentState(appointment, "BOOKED", "WAITING");
    }

    // 2. Set to ONGOING (Call Patient) & Auto-Complete Previous
    @Transactional
    public ApiResponse<String> markAsOngoing(StaffAppointmentManageDTO request) {
        Appointment newPatientAppointment = validateAndGetAppointment(request.getAppointmentId(), request.getPatientId());

        if (isStatusAlready(newPatientAppointment, "ONGOING")) {
            throw new ValidationException("Appointment is already ONGOING", "ALREADY_ONGOING", HttpStatus.BAD_REQUEST);
        }

        Integer clinicId = TenantContext.getCurrentTenant();
        Long scheduleId = newPatientAppointment.getDoctorSchedule().getId();

        // --- THE "NEXT PATIENT" TRIGGER LOGIC ---
        // Find any appointment for THIS schedule that is currently marked as ONGOING
        List<Appointment> currentlyOngoing = appointmentRepo.findOngoingAppointmentsForSchedule(scheduleId, clinicId);

        // We need the COMPLETED status lookup to auto-close them
        AppointmentStatusLookup completedStatus = statusLookupRepo.findByCode("COMPLETED")
                .orElseThrow(() -> new ValidationException("COMPLETED status not configured", "CONFIG_ERROR", HttpStatus.INTERNAL_SERVER_ERROR));

        for (Appointment prevAppt : currentlyOngoing) {
            // Don't auto-complete the appointment we are currently trying to start (just in case)
            if (!prevAppt.getId().equals(newPatientAppointment.getId())) {
                log.info("Auto-completing previous appointment ID: {} for Schedule: {}", prevAppt.getId(), scheduleId);

                // 1. Update MySQL
                prevAppt.setStatus(completedStatus);
                appointmentRepo.save(prevAppt);

                // 2. Update Firebase Queue
                queueService.completePatient(
                        Long.valueOf(clinicId),
                        scheduleId,
                        prevAppt.getQueuePosition()
                );
            }
        }
        // --- END AUTO-COMPLETE LOGIC ---

        // Now, proceed to mark the newly scanned patient as ONGOING in both DB and Firebase
        return updateAppointmentState(newPatientAppointment, "ONGOING", "ONGOING");
    }

    // 3. Set to COMPLETED (Manual Finish - Optional for staff)
    @Transactional
    public ApiResponse<String> markAsCompleted(StaffAppointmentManageDTO request) {
        Appointment appointment = validateAndGetAppointment(request.getAppointmentId(), request.getPatientId());

        if (isStatusAlready(appointment, "COMPLETED")) {
            throw new ValidationException("Appointment is already COMPLETED", "ALREADY_COMPLETED", HttpStatus.BAD_REQUEST);
        }

        return updateAppointmentState(appointment, "COMPLETED", "COMPLETED");
    }

    // --- Core Logic (Updates MySQL & Firebase) ---
    private ApiResponse<String> updateAppointmentState(Appointment appointment, String dbStatusCode, String firebaseAction) {

        // 1. Update MySQL DB
        AppointmentStatusLookup newStatus = statusLookupRepo.findByCode(dbStatusCode)
                .orElseThrow(() -> new ValidationException("Status '" + dbStatusCode + "' not found.", "CONFIG_ERROR", HttpStatus.INTERNAL_SERVER_ERROR));

        appointment.setStatus(newStatus);
        appointmentRepo.save(appointment);

        // 2. Update Firebase Queue
        Long clinicId = Long.valueOf(TenantContext.getCurrentTenant());
        Long scheduleId = appointment.getDoctorSchedule().getId();
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

        return ApiResponse.success("SUCCESS", "Appointment marked as " + dbStatusCode + ".");
    }

    // --- Helpers ---
    private boolean isStatusAlready(Appointment appointment, String expectedCode) {
        return appointment.getStatus() != null && expectedCode.equals(appointment.getStatus().getCode());
    }

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