package com.dinethdf.ClinicManagementSystem.Service.Patient;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import com.dinethdf.ClinicManagementSystem.Repo.Patient.PatientDetailsRepo;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.PatientAuthService;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import com.dinethdf.ClinicManagementSystem.util.PatientValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    PatientDetailsRepo patientDetailsRepo;

    @Autowired
    private MessageSourceUtil messageSourceUtil;

    @Autowired
    PatientAuthService patientAuthService;

    @Autowired
    PatientValidator patientValidator;

    @Override
    public List<Patient> getAllPatients() {
        return patientDetailsRepo.findAll();
    }

    @Override
    public ApiResponse<Patient> getPatientDetailsByIdStaff(String id) {
        Patient patient = patientDetailsRepo.findById(Long.valueOf(id)).orElseThrow(
                () ->  new ValidationException(messageSourceUtil.getMessage("patient.not.found"),"PATIENT_NOT_FOUND"));

        return new ApiResponse<>(
                true,
                messageSourceUtil.getMessage("patient.details.get.success"),
                patient
        );
    }


    @Override
    @Transactional
    public ApiResponse<Patient> registerPatientByStaff(Patient patient) {

            patientValidator.validatePatient(patient);
        try {
            System.out.println("Patient Save in Staff");
            patient.setPatientNo(null);

            if (patient.isDependent()){
                patient.setNic(null);
                patient.setAddress(null);
                patient.setCity(null);
                patient.setDistrict(null);
                patient.setEmail(null);
                patient.setMobileNo(null);
                patient.setLandNo(null);
            }
            patient.setRegistrationDate(LocalDateTime.now());
            Patient savedPatient = patientDetailsRepo.save(patient);

            String patientNo = generatePatientNo(savedPatient.getId());
            savedPatient.setPatientNo(patientNo);
            return new ApiResponse<>(
                    true,
                    messageSourceUtil.getMessage("patient.register.success"),
                    patientDetailsRepo.save(savedPatient)
            );

        } catch (DataIntegrityViolationException e) {
            String rootCauseMessage = e.getMostSpecificCause().getMessage();

            // for Duplicate Field Error handle
            handleDataIntegrityViolation(rootCauseMessage);

            // Any other Database Level error Handle
            throw new ValidationException(messageSourceUtil.getMessage("patient.register.error"),  "Duplicate_Entry", null, HttpStatus.BAD_REQUEST);
        }
        catch (Exception e){
            System.out.println("Exception Level Error");
            System.out.println(e.getMessage());
            throw new ValidationException(messageSourceUtil.getMessage("patient.register.error"),  "SERVER_ERROR", null, HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    public ApiResponse<Patient> updatePatientByStaff(Integer id, Patient patientFromRequest) {

        Patient existingPatient = patientDetailsRepo.findById(Long.valueOf(id)).orElseThrow(
                () ->  new ValidationException(messageSourceUtil.getMessage("patient.not.found"),"PATIENT_NOT_FOUND"));

        patientValidator.validatePatient(patientFromRequest);

        // Child Account Changing Type Update

        if(existingPatient.getGuardian() != null && patientFromRequest.getGuardian() == null){
           throw  new ValidationException(messageSourceUtil.getMessage("patient.child.guardian.cannot.remove"),"CHILD_CANNOT_REMOVE_GUARDIAN");
        }

        try {
            existingPatient.setFirstName(patientFromRequest.getFirstName());
            existingPatient.setLastName(patientFromRequest.getLastName());
            existingPatient.setFullName(patientFromRequest.getFullName()); // Assuming this can be updated
            existingPatient.setDob(patientFromRequest.getDob());

            // Attributes (Enums/Integers)
            existingPatient.setEthnicity(patientFromRequest.getEthnicity());
            existingPatient.setGender(patientFromRequest.getGender());
            existingPatient.setMaritalStatus(patientFromRequest.getMaritalStatus());
            existingPatient.setBloodGroup(patientFromRequest.getBloodGroup());

            if (!existingPatient.isDependent()){
                // Contact & Address
                existingPatient.setNic(patientFromRequest.getNic());
                existingPatient.setAddress(patientFromRequest.getAddress());
                existingPatient.setCity(patientFromRequest.getCity());
                existingPatient.setDistrict(patientFromRequest.getDistrict());
                existingPatient.setEmail(patientFromRequest.getEmail());
                existingPatient.setMobileNo(patientFromRequest.getMobileNo());
                existingPatient.setLandNo(patientFromRequest.getLandNo());
            }


            // Emergency Contact
            existingPatient.setEmergencyContactName(patientFromRequest.getEmergencyContactName());
            existingPatient.setEmergencyContactRelationship(patientFromRequest.getEmergencyContactRelationship());
            existingPatient.setEmergencyContactMobile(patientFromRequest.getEmergencyContactMobile());
            existingPatient.setEmergencyContactEmail(patientFromRequest.getEmergencyContactEmail());

            // Other
            existingPatient.setProfileImg(patientFromRequest.getProfileImg());

            Patient savedPatient = patientDetailsRepo.save(existingPatient);
            return new ApiResponse<>(
                    true,
                    messageSourceUtil.getMessage("patient.update.success"),
                    patientDetailsRepo.save(savedPatient)
            );

        } catch (DataIntegrityViolationException e) {
            String rootCauseMessage = e.getMostSpecificCause().getMessage();

            // for Duplicate Field Error handle
            handleDataIntegrityViolation(rootCauseMessage);

            // Any other Database Level error Handle
            throw new ValidationException(messageSourceUtil.getMessage("patient.register.error"),  "Duplicate_Entry", null, HttpStatus.BAD_REQUEST);
        }
        catch (Exception e){
            System.out.println("Exception Level Error");
            System.out.println(e.getMessage());
            throw new ValidationException(messageSourceUtil.getMessage("patient.register.error"),  "SERVER_ERROR", null, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ApiResponse<String> approvePatient(Integer patientId) {
        Patient existingPatient = patientDetailsRepo.findById(Long.valueOf(patientId)).orElseThrow(
                () ->  new ValidationException(messageSourceUtil.getMessage("patient.not.found"),"PATIENT_NOT_FOUND"));

        if (existingPatient.getIsApproved()) {
            throw new ValidationException(messageSourceUtil.getMessage("patient.already.Approved"),  "PATIENT_ALREADY_APPROVED", null, HttpStatus.BAD_REQUEST);
        } else {
            existingPatient.setIsApproved(true);
            patientDetailsRepo.save(existingPatient);
            return new ApiResponse<>(
                    true,
                    messageSourceUtil.getMessage("patient.approve.success"),
                    existingPatient.getPatientNo()
            );
        }
    }

    @Override
    public ApiResponse<String> deletePatient(Integer patientId) {
        Patient existingPatient = patientDetailsRepo.findById(Long.valueOf(patientId)).orElseThrow(
                () ->  new ValidationException(messageSourceUtil.getMessage("patient.not.found"),"PATIENT_NOT_FOUND"));

        if (existingPatient.getIsActive()) {
            throw new ValidationException(messageSourceUtil.getMessage("patient.active.cannot.delete"),  "PATIENT_DELETE_ACTIVE", null, HttpStatus.BAD_REQUEST);

        } else {
            existingPatient.setIsApproved(true);
            patientDetailsRepo.deleteById(existingPatient.getId());
            return new ApiResponse<>(
                    true,
                    messageSourceUtil.getMessage("patient.delete.success"),
                    existingPatient.getPatientNo()
            );
        }


    }

    private String generatePatientNo(Long id) {
        int year = LocalDate.now().getYear();
        String yearSuffix = String.valueOf(year).substring(2);
        String prefix = "P" + yearSuffix;
        return prefix + String.format("%04d", id);
    }

    public void validatePatient(Patient patient) {
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

        }
        else if (patient.getDob() == null || patient.getDob().isAfter(LocalDate.now())) {
            errorCode = "INVALID_DOB";
            errorMessage = "Date of Birth cannot be in the future.";
        }
        else if ((patient.getEmail() == null || !patient.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$") ) &&  !patient.isDependent() ) {

            errorCode = "EMAIL_INVALID_FORMAT";
            errorMessage = "Email format is invalid.";

        } else if ((patient.getMobileNo() == null || !patient.getMobileNo().matches("^\\d{10}$")) && !patient.isDependent() ) {

            errorCode = "MOBILENO_INVALID";
            errorMessage = "Mobile number not valid";

        } else if ((patient.getNic() == null || patient.getNic().length() < 9 ) && !patient.isDependent() ) {
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
            if (lowerCaseMessage.contains("mobileno")) {
                throw new ValidationException(
                        messageSourceUtil.getMessage("patient.mobileNo.exists.error"),
                        "MOBILENO_DUPLICATE",
                        null,
                        HttpStatus.BAD_REQUEST
                );
            }
            // Generic duplicate error if a specific field isn't matched
            throw new ValidationException(
                    messageSourceUtil.getMessage("patient.register.error"),
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
