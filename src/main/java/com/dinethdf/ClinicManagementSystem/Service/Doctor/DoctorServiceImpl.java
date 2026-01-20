package com.dinethdf.ClinicManagementSystem.Service.Doctor;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorDetailsDto;
import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorResponseDto;
import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorUpdateDto;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Doctor;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Specialization;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserAuth;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserRole;
import com.dinethdf.ClinicManagementSystem.Repo.Doctor.SpecializationRepo;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.mapper.DoctorMapper;

import com.dinethdf.ClinicManagementSystem.Repo.Doctor.DoctorRepo;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    DoctorRepo doctorRepo;

    @Autowired
    private MessageSourceUtil messageSourceUtil;

    @Autowired
    DoctorMapper doctorMapper;

    @Autowired
    SpecializationRepo specializationRepo;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> getAllDoctors() {
        try {
            List<Doctor> doctors = doctorRepo.findAllWithSpecialization(TenantContext.getCurrentTenant());
            return doctors;
        } catch(Exception e){
            System.out.println(e.getMessage());
            throw new ValidationException(messageSourceUtil.getMessage("token.invalid"), "ERROR ", null, HttpStatus.UNAUTHORIZED);
        }

    }

    @Override
    public DoctorDetailsDto getDoctorDetailsByIdForLoging(Long id, Integer clinicId) {

        Doctor doctor = doctorRepo.findByIdAndClinicId(id, clinicId)
                .orElseThrow(() -> new ValidationException(
                        messageSourceUtil.getMessage("doctor.not.found"), "ASDf"
                ));
        return doctorMapper.toDetailsDto(doctor);
    }

    @Override
    public ApiResponse<DoctorDetailsDto> getDoctorFullDetailsById(String id) {
        Doctor doctor = doctorRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new ValidationException(
                        messageSourceUtil.getMessage("doctor.not.found"), "ASDf"
                ));

        if (!Objects.equals(doctor.getClinicId(), TenantContext.getCurrentTenant())) {
            throw new ValidationException(messageSourceUtil.getMessage("doctor.not.found"), "NOT_FOUND", null, HttpStatus.NOT_FOUND);
        }

        return ApiResponse.success(
                doctorMapper.toDetailsDto(doctor),
                messageSourceUtil.getMessage("doctor.details.get.success")
        );
    }


    @Override
    public ApiResponse<DoctorResponseDto> getDoctorDetailsByEmail(String email) {

        if (email == null || email.isEmpty()) {
            throw new ValidationException(messageSourceUtil.getMessage("token.invalid"), "INVALID_TOKEN", null, HttpStatus.UNAUTHORIZED);
        }

        Doctor doctor = doctorRepo.findByUserAuthEmailAndClinicId(email, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new ValidationException(
                        messageSourceUtil.getMessage("doctor.not.found.for.user"),
                        "DOCTOR_PROFILE_NOT_FOUND",
                        null,
                        HttpStatus.NOT_FOUND
                ));
        return ApiResponse.success(
                doctorMapper.toResponseDTO(doctor),
                messageSourceUtil.getMessage("doctor.details.get.success")
        );
    }


    @Transactional(readOnly = true)
    @Override
    public Page<DoctorResponseDto> getAllDoctorsPaginated(
            String name,
            String mobile,
            String slmc,
            String specialization,
            Pageable pageable
    ) {
        try {
            // Convert empty strings to NULL so the query ignores them
            String filterName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
            String filterMobile = (mobile != null && !mobile.trim().isEmpty()) ? mobile.trim() : null;
            String filterSlmc = (slmc != null && !slmc.trim().isEmpty()) ? slmc.trim() : null;
            String filterSpec = (specialization != null && !specialization.trim().isEmpty()) ? specialization.trim() : null;

            return doctorRepo.searchDoctorsAdvanced(
                    filterName,
                    filterMobile,
                    filterSlmc,
                    filterSpec,
                    TenantContext.getCurrentTenant(),
                    pageable
            );

        } catch (Exception e) {
            e.printStackTrace();
            throw new ValidationException(
                    messageSourceUtil.getMessage("server.error"),
                    "SERVER_ERROR",
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponseDto> searchDoctors(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<DoctorResponseDto> doctors = doctorRepo.searchDoctors(query.trim(), TenantContext.getCurrentTenant());
        return doctors;
    }

    @Override
    @Transactional
    public ApiResponse<Doctor> registerDoctor(Doctor doctor) {

        validateDoctor(doctor);

        try {
            Integer clinicId = TenantContext.getCurrentTenant();
            doctor.setClinicId(clinicId);

            UserAuth userAuth = new UserAuth();
            userAuth.setEmail(doctor.getEmail());
            userAuth.setPassword(encoder.encode(doctor.getNic()));
            userAuth.setEnable(true);
            userAuth.setClinicId(clinicId);

            UserRole doctorRole = new UserRole();
            doctorRole.setId(3);
            userAuth.setRole(doctorRole);
            doctor.setUserAuth(userAuth);

            Doctor savedDoctor = doctorRepo.save(doctor);

            savedDoctor.getUserAuth().setDetailsId(savedDoctor.getId());

            return ApiResponse.success(
                    savedDoctor,
                    messageSourceUtil.getMessage("doctor.register.success")
            );

        } catch (DataIntegrityViolationException e) {
            String rootCauseMessage = e.getMostSpecificCause().getMessage();
            handleDataIntegrityViolation(rootCauseMessage);
            throw new ValidationException(messageSourceUtil.getMessage("doctor.register.error"), "Duplicate_Entry", null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception Level Error: " + e.getMessage());
            throw new ValidationException(messageSourceUtil.getMessage("doctor.register.error"), "SERVER_ERROR", null, HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @Override
    public DoctorResponseDto editDoctorDetails(Long doctorId, DoctorUpdateDto requestDto) {

        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ValidationException("Doctor not found", "NOT_FOUND", null, HttpStatus.NOT_FOUND));

        if (!Objects.equals(doctor.getClinicId(), TenantContext.getCurrentTenant())) {
            throw new ValidationException("Doctor not found", "NOT_FOUND", null, HttpStatus.NOT_FOUND);
        }

        if(doctorRepo.existsByUserAuthEmailAndClinicIdAndIdNot(requestDto.getEmail(), TenantContext.getCurrentTenant(), doctorId))
            throw new ValidationException("Email already exists", "DUPLICATE", null, HttpStatus.BAD_REQUEST);
        if(doctorRepo.existsByNicAndClinicIdAndIdNot(requestDto.getNic(), TenantContext.getCurrentTenant(), doctorId))
            throw new ValidationException("NIC already exists", "DUPLICATE", null, HttpStatus.BAD_REQUEST);

        doctor.setFirstName(requestDto.getFirstName());
        doctor.setLastName(requestDto.getLastName());
        doctor.setNameWithInitials(requestDto.getNameWithInitials());
        doctor.setGender(requestDto.getGender());
        doctor.setDob(requestDto.getDob());
        doctor.setNic(requestDto.getNic());
        doctor.setMobileNo(requestDto.getMobileNo());
        doctor.setAddress(requestDto.getAddress());
        doctor.setCity(requestDto.getCity());
        doctor.setDistrict(requestDto.getDistrict());
        doctor.setSlmcNumber(requestDto.getSlmcNumber());
        doctor.setQualification(requestDto.getQualification());
        doctor.setExperienceYears(requestDto.getExperienceYears());
        doctor.setEmail(requestDto.getEmail());

        // 2. Update Linked UserAuth Email (Uncommented and fixed)
        if (doctor.getUserAuth() != null) {
            // Check if the new email is different from the old auth email
            if (!doctor.getUserAuth().getEmail().equals(requestDto.getEmail())) {
                doctor.getUserAuth().setEmail(requestDto.getEmail());
            }
        }

        if(requestDto.getSpecializationId() != null) {
            Specialization spec = specializationRepo.findById(requestDto.getSpecializationId())
                    .orElseThrow(() -> new ValidationException("Invalid Specialization", "INVALID_SPEC", null, HttpStatus.BAD_REQUEST));
            doctor.setSpecialization(spec);
        }


        doctor.setUpdatedDate(LocalDateTime.now());
        doctor.setUpdatedBy(getCurrentUsername());

        Doctor saved = doctorRepo.save(doctor);
        return doctorMapper.toResponseDTO(saved);
    }


    @Transactional
    public DoctorResponseDto updateDoctorStatus(Long doctorId, String status) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ValidationException("Doctor not found", "NOT_FOUND", null, HttpStatus.NOT_FOUND));

        if (!Objects.equals(doctor.getClinicId(), TenantContext.getCurrentTenant())) {
            throw new ValidationException("Doctor not found", "NOT_FOUND", null, HttpStatus.NOT_FOUND);
        }

        try {
            doctor.setAvailabilityStatus(Doctor.AvailabilityStatus.valueOf(status));
            doctor.setUpdatedDate(LocalDateTime.now());
            doctor.setUpdatedBy(getCurrentUsername());
            return doctorMapper.toResponseDTO(doctorRepo.save(doctor));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid Status. Use: AVAILABLE, ON_LEAVE, UNAVAILABLE", "INVALID_STATUS", null, HttpStatus.BAD_REQUEST);
        }
    }


    @Transactional
    public DoctorResponseDto approveDoctor(Long doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ValidationException("Doctor not found", "NOT_FOUND", null, HttpStatus.NOT_FOUND));

        if (!Objects.equals(doctor.getClinicId(), TenantContext.getCurrentTenant())) {
            throw new ValidationException("Doctor not found", "NOT_FOUND", null, HttpStatus.NOT_FOUND);
        }

        doctor.setIsApproved(true);
        doctor.setApprovedDate(LocalDateTime.now());
        doctor.setApprovedBy(getCurrentUsername());

        doctor.setUpdatedDate(LocalDateTime.now());
        doctor.setUpdatedBy(getCurrentUsername());

        return doctorMapper.toResponseDTO(doctorRepo.save(doctor));
    }

    @Override
    @Transactional
    public void deleteDoctor(Long doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ValidationException("Doctor not found", "NOT_FOUND", null, HttpStatus.NOT_FOUND));

        if (!Objects.equals(doctor.getClinicId(), TenantContext.getCurrentTenant())) {
            throw new ValidationException("Doctor not found", "NOT_FOUND", null, HttpStatus.NOT_FOUND);
        }

        doctorRepo.deleteById(doctorId);
    }


    private void validateUpdateConflicts(Long id, DoctorUpdateDto dto) {
        if (doctorRepo.existsByUserAuthEmailAndClinicIdAndIdNot(dto.getEmail(), TenantContext.getCurrentTenant(), id)) {
            throw new ValidationException(messageSourceUtil.getMessage("doctor.email.exists.error"), "EMAIL_DUPLICATE", null, HttpStatus.BAD_REQUEST);
        }
        if (doctorRepo.existsByNicAndClinicIdAndIdNot(dto.getNic(), TenantContext.getCurrentTenant(), id)) {
            throw new ValidationException(messageSourceUtil.getMessage("doctor.nic.exists.error"), "NIC_DUPLICATE", null, HttpStatus.BAD_REQUEST);
        }
        if (doctorRepo.existsBySlmcNumberAndClinicIdAndIdNot(dto.getSlmcNumber(), TenantContext.getCurrentTenant(), id)) {
            throw new ValidationException("SLMC Number already exists", "SLMC_DUPLICATE", null, HttpStatus.BAD_REQUEST);
        }
    }

    private String getCurrentUsername() {
        try {
            return org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) { return "system_user"; }
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

        }
//        else if (doctor.getMobileNo() == null || doctor.getMobileNo().matches("^\\d{10}$")) {
//
//            errorCode = "MOBILENO_INVALID";
//            errorMessage = "Mobile number not valid";
//
//        }
        else if (doctor.getNic() == null || doctor.getNic().length() < 9) {
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
            throw new ValidationException(
                    messageSourceUtil.getMessage("doctor.self.register.error"),
                    "DUPLICATE_ENTRY",
                    null,
                    HttpStatus.BAD_REQUEST
            );
        }

        if (lowerCaseMessage.contains("cannot be null")) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("validation.field.notnull"), // e.g., "A required field was left empty."
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