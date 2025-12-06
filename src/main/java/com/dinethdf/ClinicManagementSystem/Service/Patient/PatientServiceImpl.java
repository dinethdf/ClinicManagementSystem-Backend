package com.dinethdf.ClinicManagementSystem.Service.Patient;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.GuardianResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientListDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientSimpleResponseDTO;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import com.dinethdf.ClinicManagementSystem.Repo.Patient.PatientDetailsRepo;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.PatientAuthService;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import com.dinethdf.ClinicManagementSystem.util.PatientValidator;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

        patient.setPatientAuth(null);

        return ApiResponse.success(
                patient,
                messageSourceUtil.getMessage("patient.details.get.success")
        );
    }

    @Override
    public List<GuardianResponseDTO> findGuardianByNicAndFullName(String nic, String fullName) {
        List<GuardianResponseDTO> guardians = patientDetailsRepo.searchGuardianByNicAndFullName(nic, fullName);

        if (guardians.isEmpty()) {
            throw new ValidationException("No guardian found matching the provided details.", "GUARDIAN_NOT_FOUND");
        }
        return guardians;
    }

    @Override
    public List<PatientResponseDTO> findPatientBySearching(String patientNo, String nic, String fullName, String mobileNo) {

        boolean isPatientNoValid = (patientNo != null && patientNo.length() > 2);
        boolean isNicValid = (nic != null && nic.length() > 3);
        boolean isFullNameValid = (fullName != null && fullName.length() > 2);
        boolean isMobileNoValid = (mobileNo != null && mobileNo.length() > 2);


        if (!isPatientNoValid && !isNicValid && !isFullNameValid && !isMobileNoValid) {
            throw new ValidationException(
                    "Please provide at least one search term with more than 3 characters.",
                    "INSUFFICIENT_SEARCH_CRITERIA"
            );
        }

        List<PatientResponseDTO> patient = patientDetailsRepo.searchPatient(patientNo, nic, fullName, mobileNo);

        if (patient.isEmpty()) {
            throw new ValidationException("No Patient found matching the provided details.", "PATIENT_NOT_FOUND");
        }
        return patient;
    }

    public Page<PatientListDTO> getPatientList(String search, Pageable pageable) {
        Specification<Patient> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

//            predicates.add(cb.isTrue(root.get("isActive")));

            if (StringUtils.hasText(search)) {
                String searchLower = search.toLowerCase().trim();
                List<Predicate> searchPredicates = new ArrayList<>();

                try {
                    Long searchId = Long.parseLong(search);
                    searchPredicates.add(cb.equal(root.get("id"), searchId));
                } catch (NumberFormatException ignored) {

                }

                searchPredicates.add(cb.like(cb.lower(root.get("patientNo")), "%" + searchLower + "%"));

                searchPredicates.add(cb.like(cb.lower(root.get("nic")), "%" + searchLower + "%"));

                predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };


        Page<Patient> patients = patientDetailsRepo.findAll(spec, pageable);

        return patients.map(this::convertToPatientListDTO);
    }

    private PatientListDTO convertToPatientListDTO(Patient patient) {

        return new PatientListDTO(
                patient.getId(),
                patient.getPatientNo(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getMobileNo(),
                patient.getNic(),
                patient.getGender(),
                patient.getAge(), // Your helper method in Patient entity
                patient.getIsActive()
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
//            patient.setRegistrationDate(LocalDateTime.now());
            patient.setIsActive(true);
            patient.setIsApproved(true);
            Patient savedPatient = patientDetailsRepo.save(patient);

            String patientNo = generatePatientNo(savedPatient.getId());
            savedPatient.setPatientNo(patientNo);
            return ApiResponse.success(
                    patientDetailsRepo.save(savedPatient),
                    messageSourceUtil.getMessage("patient.register.success")
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

    @Transactional
    public ApiResponse<Patient> registerPatientPublic(Patient patient) {

        patientValidator.validatePublicPatientRegister(patient);
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
            return ApiResponse.success(
                    patientDetailsRepo.save(savedPatient),
                    messageSourceUtil.getMessage("patient.register.success")
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
            return ApiResponse.success(
                    patientDetailsRepo.save(savedPatient),
                    messageSourceUtil.getMessage("patient.update.success")
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
            return ApiResponse.success(
                    existingPatient.getPatientNo(),
                    messageSourceUtil.getMessage("patient.approve.success")
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
            return ApiResponse.success(
                    existingPatient.getPatientNo(),
                    messageSourceUtil.getMessage("patient.delete.success")
            );
        }
    }

    @Override
    public List<PatientSimpleResponseDTO> findPatientOnlyByNo(String patientNo) {

        // Basic validation: Ensure patientNo is not null and has some length
        if (patientNo == null || patientNo.trim().length() < 2) {
            throw new ValidationException(
                    "Please provide a valid Patient Number (at least 2 characters).",
                    "INVALID_SEARCH_CRITERIA"
            );
        }

        List<PatientSimpleResponseDTO> results = patientDetailsRepo.searchByPatientNo(patientNo);

        if (results.isEmpty()) {
            throw new ValidationException("No Patient found with this Patient Number.", "PATIENT_NOT_FOUND");
        }

        return results;
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
