package com.dinethdf.ClinicManagementSystem.Service.Patient;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.GuardianResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientListDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientSimpleResponseDTO;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.PatientAuth;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserRole;
import com.dinethdf.ClinicManagementSystem.Repo.Patient.PatientDetailsRepo;
import com.dinethdf.ClinicManagementSystem.Repo.UsersAuth.PatientRepo;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.EmailService;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import com.dinethdf.ClinicManagementSystem.util.PatientValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    PatientDetailsRepo patientDetailsRepo;

    @Autowired
    private MessageSourceUtil messageSourceUtil;

    @Autowired private PatientRepo patientAuthRepo;

    @Autowired private EmailService emailService;

    @Autowired
    PatientValidator patientValidator;

    @Override
    public List<Patient> getAllPatients() {
        // Note: For strict multi-tenancy, rely on getPatientList or ensure Hibernate Filter is enabled
        return patientDetailsRepo.findAll();
    }

    @Override
    public ApiResponse<Patient> getPatientDetailsByIdStaff(String id) {
        Patient patient = patientDetailsRepo.findById(Long.valueOf(id)).orElseThrow(
                () ->  new ValidationException(messageSourceUtil.getMessage("patient.not.found"),"PATIENT_NOT_FOUND"));

        // Multi-Tenant Security Check
        if (!Objects.equals(patient.getClinicId(), TenantContext.getCurrentTenant())) {
            throw new ValidationException(messageSourceUtil.getMessage("patient.not.found"),"PATIENT_NOT_FOUND");
        }

//        patient.setPatientAuth(null);

        return ApiResponse.success(
                patient,
                messageSourceUtil.getMessage("patient.details.get.success")
        );
    }

    @Override
    public ApiResponse<Patient> getPatientDetailsByIdAll(String id) {
        Patient patient = patientDetailsRepo.findById(Long.valueOf(id)).orElseThrow(
                () ->  new ValidationException(messageSourceUtil.getMessage("patient.not.found"),"PATIENT_NOT_FOUND"));

        // Multi-Tenant Security Check
        if (!Objects.equals(patient.getClinicId(), TenantContext.getCurrentTenant())) {
            throw new ValidationException(messageSourceUtil.getMessage("patient.not.found"),"PATIENT_NOT_FOUND");
        }

//        patient.setPatientAuth(null);

        return ApiResponse.success(
                patient,
                messageSourceUtil.getMessage("patient.details.get.success")
        );
    }

    @Transactional(readOnly = true)
    @Override
    public  PatientResponseDTO getPatientByIdForLogin(Long id, Integer clinicId) {
        // Find by ID AND Clinic ID (Security)
        return patientDetailsRepo.findPatientDetailByIdAndClinicId(id, clinicId)
                .orElseThrow(() -> new ValidationException(
                        "Patient not found",
                        "NOT_FOUND",
                        null,
                        HttpStatus.NOT_FOUND
                ));
    }

    @Override
    public List<GuardianResponseDTO> findGuardianByNicAndFullName(String nic, String fullName) {
        // Updated to pass Clinic ID
        List<GuardianResponseDTO> guardians = patientDetailsRepo.searchGuardianByNicAndFullName(nic, fullName, TenantContext.getCurrentTenant());

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

        // Updated to pass Clinic ID
        List<PatientResponseDTO> patient = patientDetailsRepo.searchPatient(patientNo, nic, fullName, mobileNo, TenantContext.getCurrentTenant());

        if (patient.isEmpty()) {
            throw new ValidationException("No Patient found matching the provided details.", "PATIENT_NOT_FOUND");
        }
        return patient;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<PatientListDTO> getAllPatientsPaginated(
            String patientNo,
            String nic,
            String mobile,
            String name,
            Pageable pageable
    ) {
        try {
            // Helper to convert empty strings "" to null, so the query ignores them
            String filterNo = (patientNo != null && !patientNo.trim().isEmpty()) ? patientNo.trim() : null;
            String filterNic = (nic != null && !nic.trim().isEmpty()) ? nic.trim() : null;
            String filterMobile = (mobile != null && !mobile.trim().isEmpty()) ? mobile.trim() : null;
            String filterName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;

            return patientDetailsRepo.searchPatientsAdvanced(
                    filterNo,
                    filterNic,
                    filterMobile,
                    filterName,
                    TenantContext.getCurrentTenant(),
                    pageable
            );

        } catch (Exception e) {
            e.printStackTrace();
            throw new ValidationException(
                    "Error fetching patient list",
                    "SERVER_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

//    public Page<PatientListDTO> getPatientList(String search, Pageable pageable) {
//        Specification<Patient> spec = (root, query, cb) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            // Multi-Tenant Filter
//            predicates.add(cb.equal(root.get("clinicId"), TenantContext.getCurrentTenant()));
//
////            predicates.add(cb.isTrue(root.get("isActive")));
//
//            if (StringUtils.hasText(search)) {
//                String searchLower = search.toLowerCase().trim();
//                List<Predicate> searchPredicates = new ArrayList<>();
//
//                try {
//                    Long searchId = Long.parseLong(search);
//                    searchPredicates.add(cb.equal(root.get("id"), searchId));
//                } catch (NumberFormatException ignored) {
//
//                }
//
//                searchPredicates.add(cb.like(cb.lower(root.get("fullName")), "%" + searchLower + "%"));
//
//                searchPredicates.add(cb.like(cb.lower(root.get("patientNo")), "%" + searchLower + "%"));
//
//                searchPredicates.add(cb.like(cb.lower(root.get("nic")), "%" + searchLower + "%"));
//
//                predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
//            }
//
//            return cb.and(predicates.toArray(new Predicate[0]));
//        };
//
//
//        Page<Patient> patients = patientDetailsRepo.findAll(spec, pageable);
//
//        return patients.map(this::convertToPatientListDTO);
//    }
//
//    private PatientListDTO convertToPatientListDTO(Patient patient) {
//
//        return new PatientListDTO(
//                patient.getId(),
//                patient.getPatientNo(),
//                patient.getFirstName(),
//                patient.getLastName(),
//                patient.getMobileNo(),
//                patient.getNic(),
//                patient.getGender(),
//                patient.getAge(), // Your helper method in Patient entity
//                patient.getIsActive()
//        );
//    }

    @Override
    @Transactional
    public ApiResponse<Patient> registerPatientByStaff(Patient patient) {

        patientValidator.validatePatient(patient);
        try {
            // Set Clinic ID
            patient.setClinicId(TenantContext.getCurrentTenant());

            System.out.println("Patient Save in Staff");
            System.out.println(patient.toString());
            patient.setPatientNo(null);

            if (patient.isDependent()){
                patient.setNic(null);
                patient.setAddress(null);
                patient.setCity(null);
                patient.setDistrict(null);
                patient.setEmail(null);
                patient.setMobileNo(patient.getGuardian().getMobileNo()); // need add mobile No for the filter thigns
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

    @Override
    @Transactional
    public ApiResponse<Patient> registerTempPatientByStaff(Patient patient) {

        patientValidator.validateTempPatientRegister(patient);

        try {
            patient.setClinicId(TenantContext.getCurrentTenant());

            if (patient.getNic() != null && patient.getNic().trim().isEmpty()) {
                patient.setNic(null);
            }
            if (patient.getEmail() != null && patient.getEmail().trim().isEmpty()) {
                patient.setEmail(null);
            }
            if (patient.getAddress() != null && patient.getAddress().trim().isEmpty()) {
                patient.setAddress(null);
            }

            if (patient.getDob() == null) {
                patient.setDob(LocalDate.now());
            }
            if (patient.getFullName() == null || patient.getFullName().trim().isEmpty()) {
                patient.setFullName(patient.getFirstName() + " " + patient.getLastName());
            }

            // 4. Default Settings
            patient.setPatientNo(null);
            patient.setIsActive(true);
            patient.setIsApproved(true);
            patient.setIsTemp(true);

            // 5. Initial Save
            Patient savedPatient = patientDetailsRepo.save(patient);

            // 6. Generate Patient No and Update
            String patientNo = generatePatientNo(savedPatient.getId());
            savedPatient.setPatientNo(patientNo);

            return ApiResponse.success(
                    patientDetailsRepo.save(savedPatient),
                    "Temporary patient registered successfully"
            );

        } catch (DataIntegrityViolationException e) {
            String rootCauseMessage = e.getMostSpecificCause().getMessage();
            handleDataIntegrityViolation(rootCauseMessage);
            throw new ValidationException("Database Error", "Duplicate_Entry", null, HttpStatus.BAD_REQUEST);
        } catch (Exception e){
            System.out.println("Exception Level Error: " + e.getMessage());
            throw new ValidationException("Server Error while registering temp patient", "SERVER_ERROR", null, HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public ApiResponse<Patient> registerPatientPublic(Patient patient) {

        patientValidator.validatePublicPatientRegister(patient);
        try {
            // Set Clinic ID
            patient.setClinicId(TenantContext.getCurrentTenant());

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

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Transactional
    public void createPatientLogin(Long patientId) {
        Integer clinicId = TenantContext.getCurrentTenant();

        Patient patient = patientDetailsRepo.findById(patientId).orElseThrow(
                () ->  new ValidationException(messageSourceUtil.getMessage("patient.not.found"),"PATIENT_NOT_FOUND"));
        // 2. Validate Email
        if (patient.getEmail() == null || patient.getEmail().trim().isEmpty()) {
            throw new ValidationException("Patient does not have an email address. Please update patient profile first.", "EMAIL_REQUIRED", HttpStatus.BAD_REQUEST);
        }

        // 3. Check if Login already exists
        if (patientAuthRepo.findByEmailAndClinicId(patient.getEmail(), clinicId) != null) {
            throw new ValidationException("Login account already exists for this patient.", "ACCOUNT_EXISTS", HttpStatus.BAD_REQUEST);
        }

        // 4. Generate 5-Digit Temp Password
        String tempPassword = String.valueOf((int) (Math.random() * 90000) + 10000); // 10000 to 99999

        // 5. Create Patient Auth Entity
        PatientAuth newAuth = new PatientAuth();
        newAuth.setClinicId(clinicId);
        newAuth.setEmail(patient.getEmail());
        newAuth.setPassword(encoder.encode(tempPassword)); // Hash it
        newAuth.setEnable(true); // Active by default
        newAuth.setIsTempCredential(true);

        UserRole patientRole = new UserRole();
        patientRole.setId(4);
        newAuth.setRole(patientRole);
        newAuth.setDetailsId(patient.getId());

        patientAuthRepo.save(newAuth);

//        patient.set

        // 6. Send Email
        String subject = "Welcome to CureGuard - Your Login Details";
        String htmlContent = "<h3>Welcome " + patient.getFirstName() + "!</h3>" +
                "<p>Your patient portal account has been created.</p>" +
                "<p><b>Username:</b> " + patient.getEmail() + "</p>" +
                "<p><b>Temporary Password:</b> <span style='font-size: 16px; font-weight: bold; color: blue;'>" + tempPassword + "</span></p>" +
                "<p>Please log in and change your password immediately.</p>";

        emailService.sendHtmlEmail(patient.getEmail(), subject, htmlContent);
    }

    @Override
    public ApiResponse<Patient> updatePatientByStaff(Integer id, Patient patientFromRequest) {

        Patient existingPatient = patientDetailsRepo.findById(Long.valueOf(id)).orElseThrow(
                () ->  new ValidationException(messageSourceUtil.getMessage("patient.not.found"),"PATIENT_NOT_FOUND"));

        // Multi-Tenant Security Check
        if (!Objects.equals(existingPatient.getClinicId(), TenantContext.getCurrentTenant())) {
            throw new ValidationException(messageSourceUtil.getMessage("patient.not.found"),"PATIENT_NOT_FOUND");
        }

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

            existingPatient.setGuardian(patientFromRequest.getGuardian());
            existingPatient.setRelationshipToGuardian(patientFromRequest.getRelationshipToGuardian());

            // Attributes (Enums/Integers)
            existingPatient.setEthnicity(patientFromRequest.getEthnicity());
            existingPatient.setGender(patientFromRequest.getGender());
            existingPatient.setMaritalStatus(patientFromRequest.getMaritalStatus());
            existingPatient.setBloodGroup(patientFromRequest.getBloodGroup());
            existingPatient.setHeight(patientFromRequest.getHeight());
            existingPatient.setWeight(patientFromRequest.getWeight());

            // ✅ FIX 2: Use if/else for cleaner logic
            if (existingPatient.isDependent()) {
                existingPatient.setNic(null);      // Throws away the "" from FE
                existingPatient.setAddress(null);  // Throws away the "45 Lake Road" from FE
                existingPatient.setCity(null);
                existingPatient.setDistrict(null);
                existingPatient.setEmail(null);
                existingPatient.setLandNo(null);

                // Safe check to inherit guardian mobile
                if (existingPatient.getGuardian() != null && existingPatient.getGuardian().getMobileNo() != null) {
                    existingPatient.setMobileNo(existingPatient.getGuardian().getMobileNo());
                }
            } else {
                // Contact & Address for normal patients
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

            if (existingPatient.getIsTemp() != null && existingPatient.getIsTemp()) {
                existingPatient.setIsTemp(false);
            }

            // ✅ FIX 3: Removed the double save() call
            Patient savedPatient = patientDetailsRepo.save(existingPatient);

            return ApiResponse.success(
                    savedPatient,
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
    public ApiResponse<Patient> updatePatientBySelf(Long id, Patient patientFromRequest) {

        Patient existingPatient = patientDetailsRepo.findById(id).orElseThrow(
                () ->  new ValidationException(messageSourceUtil.getMessage("patient.not.found"),"PATIENT_NOT_FOUND"));

        // Multi-Tenant Security Check
        if (!Objects.equals(existingPatient.getClinicId(), TenantContext.getCurrentTenant())) {
            throw new ValidationException(messageSourceUtil.getMessage("patient.not.found"),"PATIENT_NOT_FOUND");
        }

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

        // Multi-Tenant Security Check
        if (!Objects.equals(existingPatient.getClinicId(), TenantContext.getCurrentTenant())) {
            throw new ValidationException(messageSourceUtil.getMessage("patient.not.found"),"PATIENT_NOT_FOUND");
        }

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
    public ApiResponse<String> togglePatientStatus(Integer patientId) {
        Patient existingPatient = patientDetailsRepo.findById(Long.valueOf(patientId)).orElseThrow(
                () -> new ValidationException(messageSourceUtil.getMessage("patient.not.found"), "PATIENT_NOT_FOUND"));

        // Multi-Tenant Security Check
        if (!Objects.equals(existingPatient.getClinicId(), TenantContext.getCurrentTenant())) {
            throw new ValidationException(messageSourceUtil.getMessage("patient.not.found"),"PATIENT_NOT_FOUND");
        }

        // Toggle the status (If true -> make false. If false -> make true)
        boolean newStatus = !Boolean.TRUE.equals(existingPatient.getIsActive());

        existingPatient.setIsActive(newStatus);
        patientDetailsRepo.save(existingPatient);

        String message = newStatus ? "Patient activated successfully" : "Patient deactivated successfully";

        return ApiResponse.success(existingPatient.getPatientNo(), message);
    }

    @Override
    public ApiResponse<String> deletePatient(Integer patientId) {
        Patient existingPatient = patientDetailsRepo.findById(Long.valueOf(patientId)).orElseThrow(
                () ->  new ValidationException(messageSourceUtil.getMessage("patient.not.found"),"PATIENT_NOT_FOUND"));

        // Multi-Tenant Security Check
        if (!Objects.equals(existingPatient.getClinicId(), TenantContext.getCurrentTenant())) {
            throw new ValidationException(messageSourceUtil.getMessage("patient.not.found"),"PATIENT_NOT_FOUND");
        }

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

        // Updated to pass Clinic ID
        List<PatientSimpleResponseDTO> results = patientDetailsRepo.searchByPatientNo(patientNo, TenantContext.getCurrentTenant());

        if (results.isEmpty()) {
            throw new ValidationException("No Patient found with this Patient Number.", "PATIENT_NOT_FOUND");
        }

        return results;
    }


    @Override
    public Long findExistingPatientIdByNic(String nic) {

        if (nic == null || nic.trim().length() < 10) {
            throw new ValidationException(
                    "Please provide a valid NIC.",
                    "INVALID_SEARCH_CRITERIA"
            );
        }

        return patientDetailsRepo.findIdByNic(nic, TenantContext.getCurrentTenant())
                .orElse(null);
    }

    @Override
    public List<PatientResponseDTO> checkPatientExistence(String searchValue) {

        if (searchValue == null || searchValue.trim().isEmpty()) {
            throw new ValidationException("Search value is required", "MISSING_INPUT");
        }

        return patientDetailsRepo.findByNicOrMobile(
                searchValue.trim(),
                TenantContext.getCurrentTenant()
        );
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
            throw new ValidationException(
                    messageSourceUtil.getMessage("database.error.generic"),
                    "DATABASE_ERROR",
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        String lowerCaseMessage = rootCauseMessage.toLowerCase();
        System.out.println("DB Error: " + lowerCaseMessage); // Debugging

        // 1. Check for Duplicate Entry errors
        if (lowerCaseMessage.contains("duplicate entry")) {

            // FIX: Remove the single quotes from the check.
            // Just check if the message contains the column/constraint name string.
            if (lowerCaseMessage.contains("nic")) {
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

            if (lowerCaseMessage.contains("mobileno") || lowerCaseMessage.contains("mobile")) {
                throw new ValidationException(
                        messageSourceUtil.getMessage("patient.mobileNo.exists.error"),
                        "MOBILENO_DUPLICATE",
                        null,
                        HttpStatus.BAD_REQUEST
                );
            }

            // Generic duplicate error
            throw new ValidationException(
                    messageSourceUtil.getMessage("patient.register.error"),
                    "DUPLICATE_ENTRY",
                    null,
                    HttpStatus.BAD_REQUEST
            );
        }

        // 2. Check for Not-Null Constraint errors
        if (lowerCaseMessage.contains("cannot be null") || lowerCaseMessage.contains("doesn't have a default value")) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("validation.field.notnull"),
                    "NOT_NULL_VIOLATION",
                    null,
                    HttpStatus.BAD_REQUEST
            );
        }

        // 3. Fallback
        throw new ValidationException(
                messageSourceUtil.getMessage("database.error.generic"),
                "DATABASE_INTEGRITY_ERROR",
                null,
                HttpStatus.BAD_REQUEST
        );
    }


}