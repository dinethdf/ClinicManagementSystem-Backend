package com.dinethdf.ClinicManagementSystem.Service.Public;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Public.PublicBookingRequestDTO;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import com.dinethdf.ClinicManagementSystem.Repo.Appointment.AppointmentRepo;
import com.dinethdf.ClinicManagementSystem.Service.Appointment.AppointmentNotificationService;
import com.dinethdf.ClinicManagementSystem.Service.Otp.OtpService;
import com.dinethdf.ClinicManagementSystem.Service.Patient.PatientServiceImpl;
import com.dinethdf.ClinicManagementSystem.config.TenantContext; // <--- Import
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.EmailService;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class PublicBookingService {

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private PatientServiceImpl patientService;

    @Autowired
    private MessageSourceUtil messageSourceUtil;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AppointmentNotificationService appointmentNotificationService;

    @Transactional
    public ApiResponse<Long> processPublicBooking(PublicBookingRequestDTO request) {

        // 0. Capture Clinic ID (Critical for Multi-Tenancy)
        Integer clinicId = TenantContext.getCurrentTenant();

        // 1. Verify OTP Status
        if (!otpService.isVerified(request.getMobileNo())) {
            throw new ValidationException("Mobile number not verified or OTP expired.", "OTP_REQUIRED", HttpStatus.FORBIDDEN);
        }

        Long finalPatientId = request.getExistingPatientId();

        // 2. Handle New Patient Registration
        if (finalPatientId == null) {
            Patient newPatient = new Patient();
            newPatient.setFirstName(request.getFirstName());
            newPatient.setLastName(request.getLastName());
            newPatient.setNic(request.getNic());
            newPatient.setMobileNo(request.getMobileNo());

            if(request.getEmail() != null && !request.getEmail().isEmpty()){
                newPatient.setEmail(request.getEmail());
            }

            newPatient.setFullName(request.getFirstName() + " " + request.getLastName());
            newPatient.setDob(request.getDob());
            newPatient.setGender(request.getGender());
            newPatient.setIsActive(true);
            newPatient.setIsApproved(false);

            ApiResponse<Patient> registrationResponse = patientService.registerPatientPublic(newPatient);

            if(registrationResponse.getData() == null) {
                throw new ValidationException("Failed to register new patient.", "REGISTRATION_FAILED", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            finalPatientId = registrationResponse.getData().getId();
        }

        String createdBy = "PUBLIC_WEB";

        Map<String, Object> spResult = appointmentRepo.sp_BookAppointment(
                clinicId, // <--- Added Param
                createdBy,
                request.getScheduleId(),
                finalPatientId,
                request.getNote()
        );

        String statusMessage = (String) spResult.get("o_statusMessage");
        if (!"BOOKED_SUCCESS".equals(statusMessage)) {
            handleSpError(statusMessage);
        }

        Object newIdObj = spResult.get("o_newAppointmentId");

        if (newIdObj == null) {
            throw new ValidationException("Database failed to return a valid Appointment ID", "DB_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Long newAppointmentId = ((Number) newIdObj).longValue();

        // 4. Security: Invalidate OTP so it can't be used again
        otpService.invalidateOtp(request.getMobileNo());
        System.out.println("email send Confirm " + request.getEmail());

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            System.out.println("Initiating email thread for: " + request.getEmail());

            // Start background thread
            new Thread(() -> {
                try {
                    // CRITICAL: We must MANUALLY set the Tenant Context for this new thread.
                    // Threads do not inherit ThreadLocal variables by default.
                    // If we don't do this, the DB queries inside sendBookingConfirmation will fail (clinic_id will be null).
                    TenantContext.setCurrentTenant(clinicId);

                    Thread.sleep(2000); // Wait for DB commit
                    appointmentNotificationService.sendBookingConfirmation(newAppointmentId, request.getEmail());

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Email thread interrupted");
                } catch (Exception e) {
                    System.err.println("Failed to send confirmation email: " + e.getMessage());
                } finally {
                    // Always clear context in the thread
                    TenantContext.clear();
                }
            }).start();
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
                throw new ValidationException(messageSourceUtil.getMessage("appointment.error.duplicate"), "DUPLICATE_PATIENT", HttpStatus.CONFLICT);
            case "ERROR_INVALID_SCHEDULE":
                throw new ValidationException(messageSourceUtil.getMessage("appointment.error.invalid_schedule"), "INVALID_SCHEDULE", HttpStatus.NOT_FOUND);
            case "ERROR_SCHEDULE_INACTIVE":
                throw new ValidationException(messageSourceUtil.getMessage("appointment.error.inactive_schedule"), "INACTIVE_SCHEDULE", HttpStatus.BAD_REQUEST);
            default:
                throw new ValidationException("Booking Failed: " + statusMessage, "DATABASE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}