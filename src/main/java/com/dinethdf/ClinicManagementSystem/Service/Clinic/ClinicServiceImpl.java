package com.dinethdf.ClinicManagementSystem.Service.Clinic;

import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicResponseDTO;

import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicUpdateDTO;

import com.dinethdf.ClinicManagementSystem.DTO.Clinic.PlanChangeDTO;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.Clinic;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.ClinicSettings;
import com.dinethdf.ClinicManagementSystem.Repo.Clinic.ClinicRepository;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.config.multitenant.ClinicRoutingInfo;
import com.dinethdf.ClinicManagementSystem.config.multitenant.ClinicRoutingRegistry;
import com.dinethdf.ClinicManagementSystem.config.multitenant.TenantSchemaMode;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class ClinicServiceImpl implements ClinicService {

    private final ClinicRepository clinicRepository;
    private final ClinicRoutingRegistry clinicRoutingRegistry;

    @Autowired
    @Lazy
    private ClinicService self;

    @Autowired
    public ClinicServiceImpl(ClinicRepository clinicRepository, ClinicRoutingRegistry clinicRoutingRegistry) {
        this.clinicRepository = clinicRepository;
        this.clinicRoutingRegistry = clinicRoutingRegistry;
    }

    @Override
    public String getCurrentEncryptedSecrets() {
        Clinic clinic = self.getClinicById(TenantContext.getCurrentTenant());
        return clinic.getEncryptedSecrets();
    }

    @CacheEvict(value = "clinicByIdCache", keyGenerator = "tenantIdKeyGenerator")
    public void updateEncryptedSecrets(Map<String, String> incomingSecrets) {
        Clinic clinic = getCurrentClinicEntity();

        // 1. Get the current secrets (This string is automatically decrypted by your
        // Converter)
        String existingSecretsJson = clinic.getEncryptedSecrets();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> mergedSecrets = new HashMap<>();

        try {
            // 2. If secrets already exist, parse them into our Map
            if (existingSecretsJson != null && !existingSecretsJson.trim().isEmpty()) {
                mergedSecrets = mapper.readValue(existingSecretsJson, new TypeReference<Map<String, String>>() {
                });
            }

            // 3. MERGE THE DATA!
            // .putAll() will add brand new keys, and overwrite existing keys with the same
            // name.
            mergedSecrets.putAll(incomingSecrets);

            // 4. Convert the merged map back to a plain JSON string
            String finalJsonString = mapper.writeValueAsString(mergedSecrets);

            // 5. Save it! (Hibernate will automatically encrypt finalJsonString before it
            // hits MySQL)
            clinic.setEncryptedSecrets(finalJsonString);
            clinicRepository.save(clinic);

        } catch (Exception e) {
            throw new RuntimeException("Failed to merge secure vault data", e);
        }
    }

    @Override
    public ClinicResponseDTO getCurrentClinicDetails() {
        return ClinicResponseDTO.fromEntity(self.getClinicById(TenantContext.getCurrentTenant()));
    }

    @CacheEvict(value = "clinicByIdCache", keyGenerator = "tenantIdKeyGenerator")
    public ClinicResponseDTO updateCurrentClinicDetails(ClinicUpdateDTO request) {
        Clinic clinic = getCurrentClinicEntity();

        // Partial Update: Only update if value is provided
        if (request.getName() != null)
            clinic.setName(request.getName());

        // --- UPDATED CONTACT FIELDS ---
        if (request.getMobileNo1() != null)
            clinic.setMobileNo1(request.getMobileNo1());
        if (request.getMobileNo2() != null)
            clinic.setMobileNo2(request.getMobileNo2());
        if (request.getLandlineNo() != null)
            clinic.setLandlineNo(request.getLandlineNo());

        if (request.getAddress() != null)
            clinic.setAddress(request.getAddress());
        if (request.getCity() != null)
            clinic.setCity(request.getCity());
        if (request.getDistrict() != null)
            clinic.setDistrict(request.getDistrict());
        if (request.getLogoUrl() != null)
            clinic.setLogoUrl(request.getLogoUrl());

        Clinic updated = clinicRepository.save(clinic);
        return ClinicResponseDTO.fromEntity(updated);
    }

    @CacheEvict(value = "clinicByIdCache", keyGenerator = "tenantIdKeyGenerator")
    public ClinicResponseDTO updateCurrentClinicSettings(ClinicSettings request) {
        Clinic clinic = getCurrentClinicEntity();

        // Get existing settings or create new if null
        ClinicSettings settings = clinic.getSettings();
        if (settings == null)
            settings = new ClinicSettings();

        // Update JSON Fields
        if (request.getCurrency() != null)
            settings.setCurrency(request.getCurrency());
        if (request.getTimezone() != null)
            settings.setTimezone(request.getTimezone());
        if (request.getInvoicePrefix() != null)
            settings.setInvoicePrefix(request.getInvoicePrefix());
        if (request.getLanguage() != null)
            settings.setLanguage(request.getLanguage());
        if (request.getTaxRate() != null)
            settings.setTaxRate(request.getTaxRate());
        if (request.getMultiCurrency() != null)
            settings.setMultiCurrency(request.getMultiCurrency());
        if (request.getSecondaryLanguage() != null)
            settings.setSecondaryLanguage(request.getSecondaryLanguage());
        if (request.getAppointmentBookedSmsGateway() != null)
            settings.setAppointmentBookedSmsGateway(request.getAppointmentBookedSmsGateway());
        if (request.getAppointmentConfirmSmsGateway() != null)
            settings.setAppointmentConfirmSmsGateway(request.getAppointmentConfirmSmsGateway());
        if (request.getOtpSmsGateway() != null)
            settings.setOtpSmsGateway(request.getOtpSmsGateway());

        clinic.setSettings(settings); // Re-set to ensure dirty check triggers
        Clinic updated = clinicRepository.save(clinic);
        return ClinicResponseDTO.fromEntity(updated);
    }

    /**
     * Update Subscription Plan
     */
    @CacheEvict(value = "clinicByIdCache", keyGenerator = "tenantIdKeyGenerator")
    public ClinicResponseDTO updateSubscriptionPlan(PlanChangeDTO request) {
        Clinic clinic = getCurrentClinicEntity();

        String newPlan = request.getNewPlanCode();
        if (newPlan == null || newPlan.isEmpty()) {
            throw new ValidationException("Plan code is required", "INVALID_PLAN");
        }

        clinic.setPlanCode(newPlan);

        // Logic: If moving from TRIAL to a paid plan, activate it
        if (!"TRIAL".equals(clinic.getStatus().name())) {
            clinic.setStatus(Clinic.ClinicStatus.ACTIVE);
        }

        Clinic updated = clinicRepository.save(clinic);
        return ClinicResponseDTO.fromEntity(updated);
    }

    // Helper to get current clinic safely
    private Clinic getCurrentClinicEntity() {
        Integer clinicId = TenantContext.getCurrentTenant();
        if (clinicId == null) {
            throw new ValidationException("No Clinic ID found in context", "AUTH_ERROR");
        }
        return clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ValidationException("Current Clinic not found", "CLINIC_NOT_FOUND"));
    }

    // =================================================================================
    // SECTION 2: ADMIN / SYSTEM METHODS (Requires ID or Code)
    // =================================================================================

    @Override
    public Clinic createClinic(Clinic clinic) {
        clinic.setId(null); // Prevent ID injection

        if (clinicRepository.existsBySubdomain(clinic.getSubdomain())) {
            throw new RuntimeException("Subdomain '" + clinic.getSubdomain() + "' is already taken.");
        }

        if (clinic.getClinicCode() == null || clinic.getClinicCode().isEmpty()) {
            clinic.setClinicCode(UUID.randomUUID().toString());
        }

        // Set Defaults if missing
        if (clinic.getSettings() == null) {
            clinic.setSettings(new ClinicSettings());
        }
        if (clinic.getStatus() == null) {
            clinic.setStatus(Clinic.ClinicStatus.TRIAL);
        }

        // Set Trial Logic
        if (clinic.getTrialEndsAt() == null && clinic.getStatus() == Clinic.ClinicStatus.TRIAL) {
            clinic.setTrialEndsAt(LocalDateTime.now().plusDays(14));
        }

        Clinic savedClinic = clinicRepository.save(clinic);

        // --- MULTI-TENANT: Add to Routing Registry dynamically ---
        String schemaName = (savedClinic.getTenantMode() == TenantSchemaMode.SCHEMA)
                ? "clinic_" + savedClinic.getClinicCode().replace("-", "")
                : "shared";

        clinicRoutingRegistry.registerNewClinic(
                savedClinic.getId(),
                savedClinic.getClinicCode(),
                savedClinic.getName(),
                savedClinic.getSubdomain(),
                schemaName,
                savedClinic.getTenantMode() == TenantSchemaMode.SCHEMA,
                savedClinic.getPlanCode()
        );

        return savedClinic;
    }

    @Override
    public List<Clinic> getAllClinics() {
        return clinicRepository.findAll();
    }

    @Override
    @Cacheable(value = "clinicByIdCache", key = "#id")
    public Clinic getClinicById(Integer id) {
        return clinicRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Clinic not found with ID: " + id, "CLINIC_NOT_FOUND"));
    }

    @Override
    public Optional<Clinic> getClinicByCode(String clinicCode) {
        return clinicRepository.findByClinicCodeAndDeletedFalse(clinicCode);
    }

    @Override
    public Optional<Clinic> getClinicBySubdomain(String subdomain) {
        return clinicRepository.findBySubdomain(subdomain);
    }

    @Override
    @CacheEvict(value = "clinicByIdCache", allEntries = true) // Admin update: clear all to be safe
    public Clinic updateClinic(String clinicCode, Clinic clinicDetails) {
        // Find by code (Admin context)
        Clinic existingClinic = clinicRepository.findByClinicCodeAndDeletedFalse(clinicCode)
                .orElseThrow(() -> new RuntimeException("Clinic not found with code: " + clinicCode));

        // Update basic fields
        existingClinic.setName(clinicDetails.getName());

        // --- UPDATED CONTACT FIELDS (Admin Side) ---
        existingClinic.setMobileNo1(clinicDetails.getMobileNo1());
        existingClinic.setMobileNo2(clinicDetails.getMobileNo2());
        existingClinic.setLandlineNo(clinicDetails.getLandlineNo());

        existingClinic.setAddress(clinicDetails.getAddress());
        existingClinic.setCity(clinicDetails.getCity());
        existingClinic.setDistrict(clinicDetails.getDistrict());
        existingClinic.setLogoUrl(clinicDetails.getLogoUrl());

        // Update Status/Plan if provided
        if (clinicDetails.getPlanCode() != null) {
            existingClinic.setPlanCode(clinicDetails.getPlanCode());
        }
        if (clinicDetails.getStatus() != null) {
            existingClinic.setStatus(clinicDetails.getStatus());
        }

        // Update Settings (Merge or Replace logic)
        if (clinicDetails.getSettings() != null) {
            existingClinic.setSettings(clinicDetails.getSettings());
        }

        return clinicRepository.save(existingClinic);
    }

    @Override
    @CacheEvict(value = "clinicByIdCache", allEntries = true) // Safer to clear all since we only have clinicCode here
    public void deleteClinic(String clinicCode) {
        Clinic clinic = clinicRepository.findByClinicCode(clinicCode)
                .orElseThrow(() -> new RuntimeException("Clinic not found"));

        // Soft Delete
        clinic.setDeleted(true);
        clinic.setStatus(Clinic.ClinicStatus.CANCELLED);
        clinicRepository.save(clinic);
    }

    @Override
    public boolean isSubdomainTaken(String subdomain) {
        return clinicRepository.existsBySubdomain(subdomain);
    }
}
