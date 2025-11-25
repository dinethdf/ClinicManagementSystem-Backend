package com.dinethdf.ClinicManagementSystem.Service.Public;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Public.PublicBookingRequestDTO;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import com.dinethdf.ClinicManagementSystem.Repo.Appointment.AppointmentRepo;
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
    private PatientServiceImpl patientService; // Reusing your existing service

    @Autowired
    private MessageSourceUtil messageSourceUtil;

    @Transactional
    public ApiResponse<Long> processPublicBooking(PublicBookingRequestDTO request) {

        Long finalPatientId = request.getExistingPatientId();

        // --- SCENARIO: NEW PATIENT (Register & Book) ---
        if (finalPatientId == null) {

            // 1. Map DTO to Patient Entity
            Patient newPatient = new Patient();
            newPatient.setFirstName(request.getFirstName());
            newPatient.setLastName(request.getLastName());
            newPatient.setNic(request.getNic());
            newPatient.setMobileNo(request.getMobileNo());
            newPatient.setFullName(request.getFirstName() + " " + request.getLastName());
            newPatient.setDob(request.getDob());
            newPatient.setGender(request.getGender());
//            newPatient.setAddress("");

            // Default settings for Public Web Registration
            newPatient.setIsActive(true);
            newPatient.setIsApproved(false); // Admin needs to approve later?
//            newPatient.setDependent(false);  // Assuming main patient booking

            // 2. Call your existing Patient Service
            // This handles Validation + PatientNo Generation (P250001) + DB Save
            ApiResponse<Patient> registrationResponse = patientService.registerPatientPublic(newPatient);

            // 3. Get the new ID
            finalPatientId = registrationResponse.getData().getId();
        }

        // --- SCENARIO: BOOK APPOINTMENT (Using SP) ---
        String createdBy = "PUBLIC_WEB";

        Map<String, Object> spResult = appointmentRepo.sp_BookAppointment(
                createdBy,
                request.getScheduleId(),
                finalPatientId,
                request.getNote()
        );

        // Handle SP Errors (Full, Duplicate, etc.)
        String statusMessage = (String) spResult.get("o_statusMessage");
        if (!"BOOKED_SUCCESS".equals(statusMessage)) {
            handleSpError(statusMessage);
        }

        Long newAppointmentId = (Long) spResult.get("o_newAppointmentId");

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
            default:
                throw new ValidationException("Booking Failed: " + statusMessage, "DATABASE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}