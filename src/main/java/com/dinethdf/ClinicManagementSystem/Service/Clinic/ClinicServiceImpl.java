package com.dinethdf.ClinicManagementSystem.Service.Clinic;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicCompleteContextDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicFeatureDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicUpdateDTO;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.Clinic;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.ClinicFeatureView;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.ClinicSettings;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.ClinicView;
import com.dinethdf.ClinicManagementSystem.Repo.Clinic.ClinicFeatureViewRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Clinic.ClinicRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Clinic.ClinicViewRepo;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClinicServiceImpl {

    private final ClinicRepo clinicRepo;
//    private final ClinicViewRepo clinicViewRepo;
    private final MessageSourceUtil messageSourceUtil;

    private final ClinicViewRepo clinicViewRepo;
    private final ClinicFeatureViewRepo featureViewRepo; // <--- Inject this

    /**
     * THE MASTER CACHE METHOD
     * Loads Info + Settings + Features in one go.
     */
    @Cacheable(
            value = "clinic_context",
            key = "T(com.dinethdf.ClinicManagementSystem.config.TenantContext).getCurrentTenant()"
    )
    public ClinicCompleteContextDTO getFullClinicContext() {
        Integer clinicId = TenantContext.getCurrentTenant();
        log.info("📢 CACHE MISS: Building Full Context for Clinic ID {}", clinicId);

        // 1. Fetch Basic Info (From v_clinic_data)
        ClinicView infoView = clinicViewRepo.findById(clinicId)
                .orElseThrow(() -> new ValidationException("Clinic not found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        ClinicResponseDTO infoDTO = mapViewToDTO(infoView); // Reuse your existing mapper

        // 2. Fetch All Features (From v_clinic_features)
        List<ClinicFeatureView> featureViews = featureViewRepo.findAllByClinicId(clinicId);

        // 3. Convert Features to DTOs
        List<ClinicFeatureDTO> featureDTOs = featureViews.stream()
                .map(fv -> new ClinicFeatureDTO(
                        fv.getFeatureCode(),
                        fv.getFeatureName(),
                        fv.getCategory(),
                        fv.isEnabled()
                ))
                .collect(Collectors.toList());

        // 4. Build Fast Lookup Map (Key: FeatureCode, Value: Enabled)
        Map<String, Boolean> permissionMap = featureDTOs.stream()
                .collect(Collectors.toMap(ClinicFeatureDTO::getFeatureCode, ClinicFeatureDTO::isEnabled));

        // 5. Return Wrapper
        return new ClinicCompleteContextDTO(infoDTO, featureDTOs, permissionMap);
    }

//    @Cacheable(value = "clinicInfo", key = "#root.target.getCurrentTenantId()")
//    public ApiResponse<ClinicResponseDTO> getCurrentClinicDetails() {
//        Integer clinicId = getCurrentTenantId();
//
//        log.info("📢 CACHE MISS: Fetching Clinic ID {} from Database...", TenantContext.getCurrentTenant());
//
//        // 1. Fetch from VIEW (Single Query, Pre-joined)
//        ClinicView view = clinicViewRepo.findById(clinicId)
//                .orElseThrow(() -> new ValidationException("Clinic not found", "CLINIC_NOT_FOUND", HttpStatus.NOT_FOUND));
//
//        // 2. Map View to DTO
//        return ApiResponse.success(
//                mapViewToDTO(view),
//                "Clinic details retrieved successfully"
//        );
//    }
//
//    /**
//     * WRITE: Update using the Entity Table
//     * Evicts the cache so the next read is fresh
//     */
//    @Transactional
//    @CacheEvict(
//            value = "clinic_info",
//            key = "T(com.dinethdf.ClinicManagementSystem.config.TenantContext).getCurrentTenant()"
//    )
//    public ApiResponse<ClinicResponseDTO> updateClinicDetails(ClinicUpdateDTO request) {
//        Integer clinicId = getCurrentTenantId();
//
//        // 1. Load Entity (Write Model)
//        Clinic clinic = clinicRepo.findById(clinicId)
//                .orElseThrow(() -> new ValidationException("Clinic not found", "CLINIC_NOT_FOUND", HttpStatus.NOT_FOUND));
//
//        // 2. Update Basic Fields
//        if (request.getName() != null) clinic.setName(request.getName());
//        if (request.getAddress() != null) clinic.setAddress(request.getAddress());
//        if (request.getCity() != null) clinic.setCity(request.getCity());
//        if (request.getMobileNo() != null) clinic.setMobileNo(request.getMobileNo());
//
//        if (request.getInvoicePrefix() != null) {
//            // A. Check if settings object is null
//            if (clinic.getSettings() == null) {
//                clinic.setSettings(new ClinicSettings()); // Create new if missing
//            }
//
//
//            clinic.getSettings().setInvoicePrefix(request.getInvoicePrefix());
//        }
//
//        // 4. Save
//        clinicRepo.save(clinic);
//
//        // 5. Return updated data (Fetch from View to ensure consistency)
//        ClinicView updatedView = clinicViewRepo.findById(clinicId).orElseThrow();
//        return ApiResponse.success(
//                mapViewToDTO(updatedView),
//                messageSourceUtil.getMessage("clinic.update.success")
//        );
//    }

    // --- Helper to get ID from Context ---
    public Integer getCurrentTenantId() {
        return TenantContext.getCurrentTenant();
    }

    private ClinicResponseDTO mapViewToDTO(ClinicView view) {
        ClinicResponseDTO dto = new ClinicResponseDTO();

        // Basic Fields
        dto.setId(view.getId());
        dto.setName(view.getName());
        dto.setSubdomain(view.getSubdomain());
        dto.setEmail(view.getEmail());
        dto.setMobileNo(view.getMobileNo());
        dto.setLogoUrl(view.getLogoUrl());

        // Status & Dates
        dto.setStatus(view.getStatus());
        dto.setActualStatus(view.getActualStatus());
        dto.setDaysRemaining(view.getDaysRemaining());

        // Plan
        dto.setSubscriptionPlan(view.getPlanName()); // Maps string to string

        // Settings (Check for null to avoid NullPointerException)
        if (view.getSettings() != null) {
            dto.setCurrency(view.getSettings().getCurrency());
            dto.setTimezone(view.getSettings().getTimezone());
            dto.setDateFormat(view.getSettings().getDateFormat());
            dto.setInvoicePrefix(view.getSettings().getInvoicePrefix());
        }

        return dto;
    }
}