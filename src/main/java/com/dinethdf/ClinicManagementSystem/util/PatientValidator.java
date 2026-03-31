package com.dinethdf.ClinicManagementSystem.util;


import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import com.dinethdf.ClinicManagementSystem.Repo.Patient.PatientDetailsRepo;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PatientValidator {

    private final PatientDetailsRepo patientDetailsRepo;
    private final MessageSourceUtil messageSourceUtil;

    public PatientValidator(PatientDetailsRepo patientDetailsRepo, MessageSourceUtil messageSourceUtil) {
        this.patientDetailsRepo = patientDetailsRepo;
        this.messageSourceUtil = messageSourceUtil;
    }

    /**
     * Validate all patient fields guardian rules
     */
    public void validatePatient(Patient patient) {

        // this order need to proper validation dont Change
        validateBasicFields(patient);
        validateGuardianRules(patient);
    }

    /**
     * Validate basic patient fields
     */
    private void validateBasicFields(Patient patient) {
        if (patient.getFirstName() == null || patient.getFirstName().trim().isEmpty()) {
            throw new ValidationException("First name is required.", "FIRSTNAME_EMPTY", null, HttpStatus.BAD_REQUEST);
        }
        if (patient.getLastName() == null || patient.getLastName().trim().isEmpty()) {
            throw new ValidationException("Last name is required.", "LASTNAME_EMPTY", null, HttpStatus.BAD_REQUEST);
        }
        if (patient.getFullName() == null || patient.getFullName().trim().isEmpty()) {
            throw new ValidationException("Full name is required.", "FULLNAME_EMPTY", null, HttpStatus.BAD_REQUEST);
        }
        if (patient.getDob() == null || patient.getDob().isAfter(LocalDate.now())) {
            throw new ValidationException("Date of Birth cannot be in the future.", "INVALID_DOB", null, HttpStatus.BAD_REQUEST);
        }

        // Validate guardian requirement based on patient type
        if(patient.isGuardianRequired() && !patient.isDependent()){
            throw new ValidationException(messageSourceUtil.getMessage("patient.child.require.guardian"),  "REQUIRED_GUARDIAN", null, HttpStatus.BAD_REQUEST);
        }

        // Validate email, mobile, NIC only for non-dependent patients
        if (!patient.isDependent()) {
            if ((patient.getEmail() != null && !patient.getEmail().trim().isEmpty()) && !patient.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new ValidationException("Email format is invalid.", "EMAIL_INVALID_FORMAT", null, HttpStatus.BAD_REQUEST);
            }
            if (patient.getMobileNo() == null || !patient.getMobileNo().matches("^\\d{10}$")) {
                throw new ValidationException("Mobile number not valid.", "MOBILENO_INVALID", null, HttpStatus.BAD_REQUEST);
            }
            if (patient.getNic() == null || patient.getNic().length() < 9) {
                throw new ValidationException("NIC must be at least 9 characters.", "NIC_INVALID", null, HttpStatus.BAD_REQUEST);
            }
            if (patient.getAddress() == null || patient.getAddress().length() < 5) {
                throw new ValidationException("Address must be at least 5 characters.", "ADDRESS_INVALID", null, HttpStatus.BAD_REQUEST);
            }
//            if (patient.getCity() == null || patient.getCity().trim().isEmpty()) {
//                throw new ValidationException("City is required.", "CITY_REQUIRED", null, HttpStatus.BAD_REQUEST);
//            }
//            if (patient.getDistrict() == null || patient.getDistrict().trim().isEmpty()) {
//                throw new ValidationException("District is required.", "DISTRICT_REQUIRED", null, HttpStatus.BAD_REQUEST);
//            }
        }
    }

    /**
     * Validate guardian / dependent rules
     */
    private void validateGuardianRules(Patient patient) {
        boolean guardianRequired = patient.isGuardianRequired();
        boolean hasGuardian = patient.isDependent();

        // Validate guardian requirement based on patient type
        if(guardianRequired && !hasGuardian){
            throw new ValidationException(messageSourceUtil.getMessage("patient.child.require.guardian"),  "REQUIRED_GUARDIAN", null, HttpStatus.BAD_REQUEST);
        } else if (!guardianRequired && hasGuardian){
            throw new ValidationException(messageSourceUtil.getMessage("patient.older.not.require.guardian"),  "NOT_REQUIRED_GUARDIAN", null, HttpStatus.BAD_REQUEST);
        }

        // Validate guardian if present
        if (hasGuardian) {
            System.out.println("gurdian has child Count");
            System.out.println("IS gurdian OWn User");

            Patient guardian = patientDetailsRepo.findById(patient.getGuardian().getId()).orElseThrow(
                    () ->  new ValidationException(messageSourceUtil.getMessage("patient.guardian.not.found"),"PATIENT_GUARDIAN_NOT_FOUND"));

            // Guardian cannot have another guardian
            if (guardian.getGuardian() != null){
                throw new ValidationException(messageSourceUtil.getMessage("patient.guardian.cannot.have.guardian"),  "GUARDIAN_SHOULD_BE_OWNER", null, HttpStatus.BAD_REQUEST);
            }

            long dependentCount = patientDetailsRepo.countByGuardian(guardian);
            // Limit number of dependents to 8
            if (dependentCount > 8){
                throw new ValidationException(messageSourceUtil.getMessage("patient.guardian.max.child"),  "GUARDIAN_MAX_CHILD", null, HttpStatus.BAD_REQUEST);
            }
        }
    }

    public void validatePublicPatientRegister(Patient patient) {
        if (patient.getFirstName() == null || patient.getFirstName().trim().isEmpty()) {
            throw new ValidationException("First name is required.", "FIRSTNAME_EMPTY", null, HttpStatus.BAD_REQUEST);
        }
        if (patient.getLastName() == null || patient.getLastName().trim().isEmpty()) {
            throw new ValidationException("Last name is required.", "LASTNAME_EMPTY", null, HttpStatus.BAD_REQUEST);
        }
        if (patient.getDob() == null || patient.getDob().isAfter(LocalDate.now())) {
            throw new ValidationException("Date of Birth cannot be in the future.", "INVALID_DOB", null, HttpStatus.BAD_REQUEST);
        }

        // Validate guardian requirement based on patient type
        if(patient.isGuardianRequired() && !patient.isDependent()){
            throw new ValidationException(messageSourceUtil.getMessage("patient.child.require.guardian"),  "REQUIRED_GUARDIAN", null, HttpStatus.BAD_REQUEST);
        }

        // Validate email, mobile, NIC only for non-dependent patients
        if (!patient.isDependent()) {
            if ((patient.getEmail() != null && !patient.getEmail().trim().isEmpty()) && !patient.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new ValidationException("Email format is invalid.", "EMAIL_INVALID_FORMAT", null, HttpStatus.BAD_REQUEST);
            }
            if (patient.getMobileNo() == null || !patient.getMobileNo().matches("^\\d{10}$")) {
                throw new ValidationException("Mobile number not valid.", "MOBILENO_INVALID", null, HttpStatus.BAD_REQUEST);
            }
            if (patient.getNic() == null || patient.getNic().length() < 9) {
                throw new ValidationException("NIC must be at least 9 characters.", "NIC_INVALID", null, HttpStatus.BAD_REQUEST);
            }
        }
    }

    public void validateTempPatientRegister(Patient patient) {

        if (patient.getFirstName() == null || patient.getFirstName().trim().length() < 3) {
            throw new ValidationException("First name must be at least 3 characters.", "FIRSTNAME_INVALID", null, HttpStatus.BAD_REQUEST);
        }

        if (patient.getLastName() == null || patient.getLastName().trim().length() < 3) {
            throw new ValidationException("Last name must be at least 3 characters.", "LASTNAME_INVALID", null, HttpStatus.BAD_REQUEST);
        }

        if (patient.getGender() == null) {
            throw new ValidationException("Gender is required.", "GENDER_EMPTY", null, HttpStatus.BAD_REQUEST);
        }

        if (patient.getMobileNo() == null || !patient.getMobileNo().matches("^(?:0)?(7[0-9]{8})$|^\\d{10}$")) {
            throw new ValidationException("Valid 10-digit mobile number is required.", "MOBILENO_INVALID", null, HttpStatus.BAD_REQUEST);
        }

        if (patient.getDob() != null && patient.getDob().isAfter(LocalDate.now())) {
            throw new ValidationException("Date of Birth cannot be in the future.", "INVALID_DOB", null, HttpStatus.BAD_REQUEST);
        }
        if (patient.getNic() != null && !patient.getNic().trim().isEmpty() && patient.getNic().length() < 9) {
            throw new ValidationException("NIC must be at least 9 characters.", "NIC_INVALID", null, HttpStatus.BAD_REQUEST);
        }
    }
}

