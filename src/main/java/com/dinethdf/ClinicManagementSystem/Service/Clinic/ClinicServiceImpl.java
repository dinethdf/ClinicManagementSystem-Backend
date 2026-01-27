package com.dinethdf.ClinicManagementSystem.Service.Clinic;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicUpdateDTO;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.Clinic;
import com.dinethdf.ClinicManagementSystem.Repo.Clinic.ClinicRepo;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClinicServiceImpl {

    private final ClinicRepo clinicRepo;
    private final MessageSourceUtil messageSourceUtil;

    /**
     * Get details of the currently logged-in Clinic (Tenant)
     */
    public ApiResponse<ClinicResponseDTO> getCurrentClinicDetails() {
        Integer clinicId = TenantContext.getCurrentTenant();

        Clinic clinic = clinicRepo.findById(clinicId)
                .orElseThrow(() -> new ValidationException("Clinic not found", "CLINIC_NOT_FOUND", HttpStatus.NOT_FOUND));

        return ApiResponse.success(
                mapToResponseDTO(clinic),
                "Clinic details retrieved successfully"
        );
    }

    /**
     * Update Clinic Profile (Address, Contact info, etc.)
     */
    @Transactional
    public ApiResponse<ClinicResponseDTO> updateClinicDetails(ClinicUpdateDTO request) {
        Integer clinicId = TenantContext.getCurrentTenant();

        Clinic clinic = clinicRepo.findById(clinicId)
                .orElseThrow(() -> new ValidationException("Clinic not found", "CLINIC_NOT_FOUND", HttpStatus.NOT_FOUND));

        // Update fields only if they are provided (not null)
        if (request.getName() != null) clinic.setName(request.getName());
        if (request.getAddress() != null) clinic.setAddress(request.getAddress());
        if (request.getCity() != null) clinic.setCity(request.getCity());
        if (request.getDistrict() != null) clinic.setDistrict(request.getDistrict());
        if (request.getMobileNo() != null) clinic.setMobileNo(request.getMobileNo());
        if (request.getLandNo() != null) clinic.setLandNo(request.getLandNo());
        if (request.getEmail() != null) clinic.setEmail(request.getEmail());
        if (request.getWebsite() != null) clinic.setWebsite(request.getWebsite());

        // Update timestamp is handled by @PreUpdate in Entity
        Clinic savedClinic = clinicRepo.save(clinic);

        return ApiResponse.success(
                mapToResponseDTO(savedClinic),
                messageSourceUtil.getMessage("clinic.update.success")
        );
    }

    // --- Helper Method for Manual Mapping ---
    private ClinicResponseDTO mapToResponseDTO(Clinic clinic) {
        ClinicResponseDTO dto = new ClinicResponseDTO();
        dto.setId(clinic.getId());
        dto.setName(clinic.getName());
        dto.setSubdomain(clinic.getSubdomain());

        // Contact Details
        dto.setAddress(clinic.getAddress());
        dto.setCity(clinic.getCity());
        dto.setMobileNo(clinic.getMobileNo());
        dto.setLandNo(clinic.getLandNo());
        dto.setEmail(clinic.getEmail());

        // Branding & Status
        dto.setLogoUrl(clinic.getLogoUrl());
        dto.setStatus(clinic.getStatus().name()); // Convert Enum to String
        dto.setSubscriptionPlan(clinic.getSubscriptionPlan());
        dto.setExpiryDate(clinic.getExpiryDate());

        return dto;
    }
}