package com.dinethdf.ClinicManagementSystem.Service.Patient;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientPortalBookingRequestDTO;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import com.dinethdf.ClinicManagementSystem.Repo.Appointment.AppointmentRepo;

import com.dinethdf.ClinicManagementSystem.Repo.Patient.PatientDetailsRepo;
import com.dinethdf.ClinicManagementSystem.Repo.UsersAuth.PatientRepo;
import com.dinethdf.ClinicManagementSystem.Service.Appointment.AppointmentNotificationService;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class PatientPortalBookingService {

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private PatientDetailsRepo patientRepo;

    @Autowired
    private MessageSourceUtil messageSourceUtil;

    @Autowired
    private AppointmentNotificationService appointmentNotificationService;

    @Transactional
    public ApiResponse<Long> bookAppointment(Long patientId, PatientPortalBookingRequestDTO request) {

        // 0. Capture Context
        Integer clinicId = TenantContext.getCurrentTenant();

        // 1. Fetch Patient (We need the email for the notification)
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new ValidationException("Patient record not found", "PATIENT_NOT_FOUND", HttpStatus.NOT_FOUND));

        String createdBy = "PATIENT_PORTAL_BY_SELF"; // Audit trail

        // 2. Call Stored Procedure
        Map<String, Object> spResult = appointmentRepo.sp_BookAppointment(
                clinicId,
                createdBy,
                request.getScheduleId(),
                patientId,
                request.getNote()
        );

        // 3. Handle SP Response
        String statusMessage = (String) spResult.get("o_statusMessage");
        if (!"BOOKED_SUCCESS".equals(statusMessage)) {
            handleSpError(statusMessage);
        }

        Object newIdObj = spResult.get("o_newAppointmentId");
        if (newIdObj == null) {
            throw new ValidationException("Database failed to return a valid Appointment ID", "DB_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Long newAppointmentId = ((Number) newIdObj).longValue();

        if (patient.getEmail() != null && !patient.getEmail().isEmpty()) {
            appointmentNotificationService.sendBookingConfirmation(
                    newAppointmentId,
                    patient.getEmail(),
                    clinicId
            );
        }

        return ApiResponse.success(
                newAppointmentId,
                "Appointment booked successfully."
        );
    }

    private void handleSpError(String statusMessage) {
        switch (statusMessage) {
            case "ERROR_SCHEDULE_FULL":
                throw new ValidationException(messageSourceUtil.getMessage("appointment.error.full"), "SCHEDULE_FULL", HttpStatus.CONFLICT);
            case "ERROR_DUPLICATE_PATIENT":
                // For logged-in users, this means they already booked this specific slot
                throw new ValidationException("You have already booked an appointment for this schedule.", "DUPLICATE_BOOKING", HttpStatus.CONFLICT);
            case "ERROR_INVALID_SCHEDULE":
                throw new ValidationException(messageSourceUtil.getMessage("appointment.error.invalid_schedule"), "INVALID_SCHEDULE", HttpStatus.NOT_FOUND);
            case "ERROR_SCHEDULE_INACTIVE":
                throw new ValidationException(messageSourceUtil.getMessage("appointment.error.inactive_schedule"), "INACTIVE_SCHEDULE", HttpStatus.BAD_REQUEST);
            default:
                throw new ValidationException("Booking Failed: " + statusMessage, "DATABASE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}