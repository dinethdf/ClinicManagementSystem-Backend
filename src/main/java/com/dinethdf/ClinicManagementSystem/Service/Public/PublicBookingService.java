package com.dinethdf.ClinicManagementSystem.Service.Public;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Public.PublicBookingRequestDTO;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import com.dinethdf.ClinicManagementSystem.Repo.Appointment.AppointmentRepo;
import com.dinethdf.ClinicManagementSystem.Service.Otp.OtpService;
import com.dinethdf.ClinicManagementSystem.Service.Patient.PatientServiceImpl;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
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

    @Transactional
    public ApiResponse<Long> processPublicBooking(PublicBookingRequestDTO request) {

     //   // Uncomment this if/when you are ready to enable OTP
        if (!otpService.isVerified(request.getMobileNo())) {
            return ApiResponse.error("Please verify your mobile number before booking.");
        }

        Long finalPatientId = request.getExistingPatientId();

        if (finalPatientId == null) {

            Patient newPatient = new Patient();
            newPatient.setFirstName(request.getFirstName());
            newPatient.setLastName(request.getLastName());
            newPatient.setNic(request.getNic());
            newPatient.setMobileNo(request.getMobileNo());
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

        return ApiResponse.success(
                newAppointmentId,
                "Appointment booked successfully."
        );
    }

    // Ensure you have the error handler in this class as well
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