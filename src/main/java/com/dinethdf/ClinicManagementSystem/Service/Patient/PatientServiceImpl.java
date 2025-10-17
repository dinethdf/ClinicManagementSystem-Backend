package com.dinethdf.ClinicManagementSystem.Service.Patient;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientRegistrationDto;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientResponseDto;
import com.dinethdf.ClinicManagementSystem.Model.Patient.PatientDetails;
import com.dinethdf.ClinicManagementSystem.Repo.Patient.PatientDetailsRepo;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.PatientAuthService;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    PatientDetailsRepo patientDetailsRepo;

    @Autowired
    private MessageSourceUtil messageSourceUtil;

    @Autowired
    PatientAuthService patientAuthService;

    @Override
    public List<PatientResponseDto> getAllPatients() {
        return List.of();
    }

    @Override
    public PatientResponseDto getPatientById(Long patientId) {
        return null;
    }

    @Override
    public ApiResponse<PatientDetails> getPatientFullDetailsById(String id) {

        PatientDetails patientDetails = patientDetailsRepo.findById(Long.valueOf(id)).orElseThrow(() ->  new ValidationException(messageSourceUtil.getMessage("patient.not.found"),"ASDf"));
        return new ApiResponse<>(
                true,
                messageSourceUtil.getMessage("patient.details.get.success"),
                patientDetails
        );

    }

    @Override
    public PatientResponseDto getPatientByAuthId(Long authId) {
        return null;
    }

    @Override
    public PatientResponseDto registerPatientDetailsSelf(PatientRegistrationDto requestDto) {
        return null;
    }

    @Override
    @Transactional
    public ApiResponse<PatientDetails> registerPatientByStaff(PatientDetails patientDetails) {
        System.out.println(patientDetails.toString());
        validatePatient(patientDetails);
        try {
            System.out.println("Patient Save in Staff");
            patientDetails.setPatientNo(null);
            PatientDetails savedPatient = patientDetailsRepo.save(patientDetails);

            String patientNo = generatePatientNo(savedPatient.getId());
            savedPatient.setPatientNo(patientNo);
            return new ApiResponse<>(
                true,
                 messageSourceUtil.getMessage("patient.self.register.success"),
                 patientDetailsRepo.save(savedPatient)
        );

        } catch (DataIntegrityViolationException e) {
            String rootCauseMessage = e.getMostSpecificCause().getMessage();

            // for Duplicate Field Error handle
            handleDataIntegrityViolation(rootCauseMessage);

            // Any other Database Level error Handle
            throw new ValidationException(messageSourceUtil.getMessage("patient.self.register.error"),  "Duplicate_Entry", null, HttpStatus.BAD_REQUEST);
        }
        catch (Exception e){
            System.out.println("Exception Level Error");
            System.out.println(e.getMessage());
            throw new ValidationException(messageSourceUtil.getMessage("patient.self.register.error"),  "SERVER_ERROR", null, HttpStatus.BAD_REQUEST);
        }


    }


    @Override
    public PatientResponseDto editPatientDetails(Long patientId, PatientRegistrationDto requestDto) {
        return null;
    }

    @Override
    public PatientResponseDto approvePatient(Long patientId) {
        return null;
    }

    @Override
    public void deletePatient(Long patientId) {

    }

    private String generatePatientNo(Long id) {
        int year = LocalDate.now().getYear();
        String yearSuffix = String.valueOf(year).substring(2);
        String prefix = "P" + yearSuffix;
        return prefix + String.format("%04d", id);
    }

    public void validatePatient(PatientDetails patient) {
        String errorCode = null;
        String errorMessage = null;

        if (patient.getFirstName() == null || patient.getFirstName().trim().isEmpty()) {

            errorCode = "FIRSTNAME_EMPTY";
            errorMessage = "First name is required.";

        } else if (patient.getLastName() == null || patient.getLastName().trim().isEmpty()) {

            errorCode = "LASTNAME_EMPTY";
            errorMessage = "Last name is required.";

        } else if (patient.getFullName() == null || patient.getFullName().trim().isEmpty()) {

            errorCode = "FULLNAME_EMPTY";
            errorMessage = "Full name is required.";

        } else if (patient.getEmail() == null || !patient.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {

            errorCode = "EMAIL_INVALID_FORMAT";
            errorMessage = "Email format is invalid.";

        } else if (patient.getMobileNo() == null || !patient.getMobileNo().matches("^\\d{10}$")) {

            errorCode = "MOBILENO_INVALID";
            errorMessage = "Mobile number not valid";

        } else if (patient.getNic() == null || patient.getNic().length() < 9) {
            errorCode = "NIC_INVALID";
            errorMessage = "NIC must be at least 9 characters.";
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
                        messageSourceUtil.getMessage("patient.nic.exists.error"),
                        "NIC_DUPLICATE",
                        null,
                        HttpStatus.BAD_REQUEST
                );
            }
            if (lowerCaseMessage.contains("email")) {
                throw new ValidationException(
                        messageSourceUtil.getMessage("patient.email.exists.error"),
                        "EMAIL_DUPLICATE",
                        null,
                        HttpStatus.BAD_REQUEST
                );
            }
            // Generic duplicate error if a specific field isn't matched
            throw new ValidationException(
                    messageSourceUtil.getMessage("patient.self.register.error"),
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
