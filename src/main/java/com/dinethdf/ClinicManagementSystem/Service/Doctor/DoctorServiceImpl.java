package com.dinethdf.ClinicManagementSystem.Service.Doctor;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorRegistrationDto;
import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorResponseDto;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Doctor;
import com.dinethdf.ClinicManagementSystem.mapper.DoctorMapper;

import com.dinethdf.ClinicManagementSystem.Repo.Doctor.DoctorRepo;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    DoctorRepo doctorRepo;

    @Autowired
    private MessageSourceUtil messageSourceUtil;

    @Autowired
    DoctorMapper doctorMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> getAllDoctors() {
        try {
            List<Doctor> doctors = doctorRepo.findAllWithSpecialization();
            return doctors;
        } catch(Exception e){
            System.out.println(e.getMessage());
            throw new ValidationException(messageSourceUtil.getMessage("token.invalid"), "ERROR ", null, HttpStatus.UNAUTHORIZED);
        }

    }

    @Override
    public DoctorResponseDto getDoctorById(Long doctorId) {
        return null;
    }

    @Override
    public ApiResponse<DoctorResponseDto> getDoctorFullDetailsById(String id) {

        Doctor doctor = doctorRepo.findById(Long.valueOf(id)).orElseThrow(() ->  new ValidationException(messageSourceUtil.getMessage("doctor.not.found"),"ASDf"));
        return ApiResponse.success(
                doctorMapper.toResponseDTO(doctor),
                messageSourceUtil.getMessage("doctor.details.get.success")
        );

    }

    @Override
    public ApiResponse<DoctorResponseDto> getDoctorDetailsByEmail(String email) {

        if (email == null || email.isEmpty()) {
            // This should not happen if Spring Security is working, but it's good practice
            throw new ValidationException(messageSourceUtil.getMessage("token.invalid"), "INVALID_TOKEN", null, HttpStatus.UNAUTHORIZED);
        }

        // 1. Use the new repo method to find the doctor by email
        Doctor doctor = doctorRepo.findByUserAuthEmail(email)
                .orElseThrow(() -> new ValidationException(
                        messageSourceUtil.getMessage("doctor.not.found.for.user"), // e.g., "No doctor profile is linked to this user account."
                        "DOCTOR_PROFILE_NOT_FOUND",
                        null,
                        HttpStatus.NOT_FOUND
                ));

        // 2. Return the success response, just like your other function
        return ApiResponse.success(
                doctorMapper.toResponseDTO(doctor),
                messageSourceUtil.getMessage("doctor.details.get.success")
        );
    }

    @Override
    @Transactional(readOnly = true) // Mark as read-only for performance
    public List<DoctorResponseDto> searchDoctors(String query) {
        // If the query is empty, return an empty list immediately
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Call the new repository method
        List<DoctorResponseDto> doctors = doctorRepo.searchDoctors(query.trim());

        // Map the list of Doctor entities to a list of DTOs
        return doctors;
    }

    @Override
    public DoctorResponseDto registerDoctorDetailsSelf(DoctorRegistrationDto requestDto) {
        return null;
    }

    @Override
    @Transactional
    public ApiResponse<Doctor> registerDoctor(Doctor doctor) {
        System.out.println(doctor.toString());
        validateDoctor(doctor);
        try {
            Doctor savedPatient = doctorRepo.save(doctor);
            return ApiResponse.success(
                    savedPatient,
                    messageSourceUtil.getMessage("doctor.register.success")
            );

        } catch (DataIntegrityViolationException e) {
            String rootCauseMessage = e.getMostSpecificCause().getMessage();

            // for Duplicate Field Error handle
            handleDataIntegrityViolation(rootCauseMessage);

            // Any other Database Level error Handle
            throw new ValidationException(messageSourceUtil.getMessage("doctor.register.error"),  "Duplicate_Entry", null, HttpStatus.BAD_REQUEST);
        }
        catch (Exception e){
            System.out.println("Exception Level Error");
            throw new ValidationException(messageSourceUtil.getMessage("doctor.register.error"),  "SERVER_ERROR", null, HttpStatus.BAD_REQUEST);
        }


    }


    @Override
    public DoctorResponseDto editDoctorDetails(Long doctorId, DoctorRegistrationDto requestDto) {
        return null;
    }

    @Override
    public DoctorResponseDto approveDoctor(Long doctorId) {
        return null;
    }

    @Override
    public void deleteDoctor(Long doctorId) {

    }

    public void validateDoctor(Doctor doctor) {
        String errorCode = null;
        String errorMessage = null;

        if (doctor.getFirstName() == null || doctor.getFirstName().trim().isEmpty()) {

            errorCode = "FIRSTNAME_EMPTY";
            errorMessage = "First name is required.";

        } if (doctor.getLastName() == null || doctor.getLastName().trim().isEmpty()) {

            errorCode = "LASTNAME_EMPTY";
            errorMessage = "Last name is required.";

        } else if (doctor.getNameWithInitials() == null || doctor.getNameWithInitials().trim().isEmpty()) {

            errorCode = "NAMEWITHINITIALS_EMPTY";
            errorMessage = "Name with initials is required.";

        } else if (doctor.getEmail() == null || !doctor.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {

            errorCode = "EMAIL_INVALID_FORMAT";
            errorMessage = "Email format is invalid.";

        } else if (doctor.getMobileNo() == null || doctor.getMobileNo().matches("^\\d{10}$")) {

            errorCode = "MOBILENO_INVALID";
            errorMessage = "Mobile number not valid";

        } else if (doctor.getNic() == null || doctor.getNic().length() < 9) {
            errorCode = "NIC_INVALID";
            errorMessage = "NIC must be at least 9 characters.";
        }else if (doctor.getSlmcNumber() == null || doctor.getSlmcNumber().isEmpty()) {
            errorCode = "SLMC_INVALID";
            errorMessage = "SLMC Number is required.";
        }else if (doctor.getSpecialization().getId() == null) {
            errorCode = "SPECIALIZATION_EMPTY";
            errorMessage = "Specialization is required.";
        }

        if (errorCode != null) {
            throw new ValidationException(
                    errorMessage,
                    errorCode,
                    null,
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void handleDataIntegrityViolation(String rootCauseMessage) {
        if (rootCauseMessage == null) {
            // Throw a generic error if the message is null for some reason
            throw new ValidationException(
                    messageSourceUtil.getMessage("database.error.generic"),
                    "DATABASE_ERROR",
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        String lowerCaseMessage = rootCauseMessage.toLowerCase();
        System.out.println(lowerCaseMessage); // For debugging

        // 1. Check for Duplicate Entry errors
        if (lowerCaseMessage.contains("duplicate entry")) {
            if (lowerCaseMessage.contains("'nic'") || lowerCaseMessage.contains(".nic'")) {
                throw new ValidationException(
                        messageSourceUtil.getMessage("doctor.nic.exists.error"),
                        "NIC_DUPLICATE",
                        null,
                        HttpStatus.BAD_REQUEST
                );
            }
            if (lowerCaseMessage.contains("email")) {
                throw new ValidationException(
                        messageSourceUtil.getMessage("doctor.email.exists.error"),
                        "EMAIL_DUPLICATE",
                        null,
                        HttpStatus.BAD_REQUEST
                );
            }
            if (lowerCaseMessage.contains("mobileno")) {
                throw new ValidationException(
                        messageSourceUtil.getMessage("doctor.mobileNo.exists.error"),
                        "MOBILENO_DUPLICATE",
                        null,
                        HttpStatus.BAD_REQUEST
                );
            }
            // Generic duplicate error if a specific field isn't matched
            throw new ValidationException(
                    messageSourceUtil.getMessage("doctor.self.register.error"),
                    "DUPLICATE_ENTRY",
                    null,
                    HttpStatus.BAD_REQUEST
            );
        }

        // 2. Check for Not-Null Constraint errors
        if (lowerCaseMessage.contains("cannot be null")) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("validation.field.notnull"), // e.g., "A required field was left empty."
                    "NOT_NULL_VIOLATION",
                    null,
                    HttpStatus.BAD_REQUEST
            );
        }

        // 3. Fallback for any other DataIntegrityViolationException
        throw new ValidationException(
                messageSourceUtil.getMessage("database.error.generic"),
                "DATABASE_INTEGRITY_ERROR",
                null,
                HttpStatus.BAD_REQUEST
        );
    }


}
