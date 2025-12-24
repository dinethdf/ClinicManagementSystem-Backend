package com.dinethdf.ClinicManagementSystem.Service.ClinicStaff;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff.ClinicStaffResponseDto;
import com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff.ClinicStaffUpdateDto;
import com.dinethdf.ClinicManagementSystem.Model.ClinicStaff.ClinicStaff;
import com.dinethdf.ClinicManagementSystem.Model.ClinicStaff.Status;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserAuth;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserRole;
import com.dinethdf.ClinicManagementSystem.Repo.ClinicStaff.ClinicStaffRepo;
import com.dinethdf.ClinicManagementSystem.config.TenantContext; // <--- Import Context
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class ClinicStaffServiceImpl implements ClinicStaffService {

    @Autowired
    private ClinicStaffRepo clinicStaffRepo;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private MessageSourceUtil messageSourceUtil;


    @Override
    @Transactional(readOnly = true)
    public ClinicStaffResponseDto getStaffById(Long id) {
        // Find by ID AND Clinic ID (Security)
        return clinicStaffRepo.findStaffDetailByIdAndClinicId(id, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new ValidationException(
                        "Staff member not found",
                        "NOT_FOUND",
                        null,
                        HttpStatus.NOT_FOUND
                ));
    }


    @Transactional
    @Override
    public ApiResponse<ClinicStaff> registerStaff(ClinicStaff staff) {

        Integer clinicId = TenantContext.getCurrentTenant();

        // 1. Validate Input
        validateStaff(staff);

        try {
            staff.setClinicId(clinicId);
            UserAuth userAuth = new UserAuth();
            userAuth.setEmail(staff.getEmail());
            // Default password is NIC, encrypted
            userAuth.setPassword(encoder.encode(staff.getNic()));
            userAuth.setEnable(true);


            UserRole role = new UserRole();
            int roleId = (staff.getAssignedRoleId() != null) ? staff.getAssignedRoleId() : 2;

            role.setId(roleId);
            userAuth.setRole(role);
            userAuth.setClinicId(clinicId);

            staff.setUserAuth(userAuth);

            if (staff.getStatus() == null) {
                staff.setStatus(Status.ACTIVE);
            }

            staff.setCreatedDate(LocalDateTime.now());
            staff.setCreatedBy("ADMIN"); // Ideally fetch from SecurityContext

            // 5. Save
            ClinicStaff savedStaff = clinicStaffRepo.save(staff);

            // 6. Link back ID (if required by your UserAuth logic)
            savedStaff.getUserAuth().setDetailsId(savedStaff.getId());

            return ApiResponse.success(
                    savedStaff,
                    messageSourceUtil.getMessage("staff.register.success")
            );

        } catch (DataIntegrityViolationException e) {
            String rootCauseMessage = e.getMostSpecificCause().getMessage();
            handleDataIntegrityViolation(rootCauseMessage);
            throw new ValidationException(messageSourceUtil.getMessage("staff.register.error"), "Duplicate_Entry", null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ValidationException(messageSourceUtil.getMessage("staff.register.error"), "SERVER_ERROR", null, HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ClinicStaffResponseDto> getAllStaffPaginated(int page, int size) {
        try {
            // Sort by ID descending (newest first)
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

            // Pass Clinic ID to Repo
            return clinicStaffRepo.findAllStaffPaginated(TenantContext.getCurrentTenant(), pageable);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ValidationException("Error retrieving staff list", "SERVER_ERROR", null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- 2. UPDATE STAFF ---
    @Transactional
    @Override
    public ClinicStaffResponseDto editStaffDetails(Long staffId, ClinicStaffUpdateDto requestDto) {

        Integer clinicId = TenantContext.getCurrentTenant();

        ClinicStaff staff = clinicStaffRepo.findById(staffId)
                .orElseThrow(() -> new ValidationException("Staff member not found", "NOT_FOUND", null, HttpStatus.NOT_FOUND));

        // SECURITY CHECK: Ensure staff belongs to this clinic
        if (!Objects.equals(staff.getClinicId(), clinicId)) {
            throw new ValidationException("Staff member not found", "NOT_FOUND", null, HttpStatus.NOT_FOUND);
        }

        // Validate duplicates within this clinic
        if(clinicStaffRepo.existsByUserAuthEmailAndIdNotAndClinicId(requestDto.getEmail(), staffId, clinicId))
            throw new ValidationException("Email already exists", "DUPLICATE", null, HttpStatus.BAD_REQUEST);

        if(clinicStaffRepo.existsByNicAndIdNotAndClinicId(requestDto.getNic(), staffId, clinicId))
            throw new ValidationException("NIC already exists", "DUPLICATE", null, HttpStatus.BAD_REQUEST);

        // Update Fields
        staff.setFirstName(requestDto.getFirstName());
        staff.setLastName(requestDto.getLastName());
        staff.setNameWithInitials(requestDto.getNameWithInitials());
        staff.setGender(requestDto.getGender());
        staff.setDob(requestDto.getDob());
        staff.setNic(requestDto.getNic());
        staff.setMobileNo(requestDto.getMobileNo());
        staff.setAddress(requestDto.getAddress());
        staff.setCity(requestDto.getCity());
        staff.setDistrict(requestDto.getDistrict());
        staff.setDesignation(requestDto.getDesignation());
        staff.setEmail(requestDto.getEmail());

        // --- CRITICAL: Update UserAuth Email ---
        if(staff.getUserAuth() != null) {
            staff.getUserAuth().setEmail(requestDto.getEmail());
        }

        // Audit Fields
        staff.setUpdatedDate(LocalDateTime.now());

        ClinicStaff saved = clinicStaffRepo.save(staff);

        // Return mapped DTO
        return new ClinicStaffResponseDto(
                saved.getId(), saved.getFirstName(), saved.getLastName(), saved.getEmail(),
                saved.getMobileNo(), saved.getCity(), saved.getNic(), saved.getDob(),
                saved.getNameWithInitials(), saved.getDesignation(), saved.getStatus(), saved.getProfileImg(), saved.getGender()
        );
    }


    public void validateStaff(ClinicStaff staff) {
        String errorCode = null;
        String errorMessage = null;

        if (staff.getFirstName() == null || staff.getFirstName().trim().isEmpty()) {
            errorCode = "FIRSTNAME_EMPTY";
            errorMessage = "First name is required.";
        } else if (staff.getLastName() == null || staff.getLastName().trim().isEmpty()) {
            errorCode = "LASTNAME_EMPTY";
            errorMessage = "Last name is required.";
        } else if (staff.getEmail() == null || !staff.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorCode = "EMAIL_INVALID_FORMAT";
            errorMessage = "Email format is invalid.";
        } else if (staff.getNic() == null || staff.getNic().length() < 9) {
            errorCode = "NIC_INVALID";
            errorMessage = "NIC must be at least 9 characters.";
        }

        // Ensure a Role ID was actually sent
        else if (staff.getAssignedRoleId() == null || staff.getAssignedRoleId() < 1) {
            errorCode = "ROLE_INVALID";
            errorMessage = "A valid User Role is required.";
        }

        if (errorCode != null) {
            throw new ValidationException(errorMessage, errorCode, null, HttpStatus.BAD_REQUEST);
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
        System.out.println(lowerCaseMessage);

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
            throw new ValidationException(
                    messageSourceUtil.getMessage("doctor.self.register.error"),
                    "DUPLICATE_ENTRY",
                    null,
                    HttpStatus.BAD_REQUEST
            );
        }

        if (lowerCaseMessage.contains("cannot be null")) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("validation.field.notnull"),
                    "NOT_NULL_VIOLATION",
                    null,
                    HttpStatus.BAD_REQUEST
            );
        }

        throw new ValidationException(
                messageSourceUtil.getMessage("database.error.generic"),
                "DATABASE_INTEGRITY_ERROR",
                null,
                HttpStatus.BAD_REQUEST
        );
    }
}