package com.dinethdf.ClinicManagementSystem.Service.SystemConfig;

import com.dinethdf.ClinicManagementSystem.Repo.SystemConfig.SystemConfigRepo;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SystemConfigService {

    @Autowired
    private SystemConfigRepo repo;

    // ─── Read Methods (Cached) ──────────────────────────────────────────────────

    /**
     * Returns true if SMS OTP sending is enabled for the current clinic.
     * Cached per-clinic — DB is only hit once per cache window.
     * Default: true (if no config row exists for this clinic).
     */
    @Cacheable(value = "systemConfigCache", key = "T(com.dinethdf.ClinicManagementSystem.config.TenantContext).getCurrentTenant() + ':SMS_OTP_ENABLED'")
    public boolean isSmsEnabled() {
        Integer clinicId = TenantContext.getCurrentTenant();
        return repo.findByClinicIdAndKey(clinicId, "SMS_OTP_ENABLED")
                .map(config -> Boolean.parseBoolean(config.getValue()))
                .orElse(true); // Default ON if not configured
    }

    /**
     * Returns true if Email OTP sending is enabled for the current clinic.
     * Cached per-clinic — DB is only hit once per cache window.
     * Default: true (if no config row exists for this clinic).
     */
    @Cacheable(value = "systemConfigCache", key = "T(com.dinethdf.ClinicManagementSystem.config.TenantContext).getCurrentTenant() + ':EMAIL_OTP_ENABLED'")
    public boolean isEmailEnabled() {
        Integer clinicId = TenantContext.getCurrentTenant();
        return repo.findByClinicIdAndKey(clinicId, "EMAIL_OTP_ENABLED")
                .map(config -> Boolean.parseBoolean(config.getValue()))
                .orElse(true); // Default ON if not configured
    }

    // ─── Write Method (Cache Evict) ─────────────────────────────────────────────

    /**
     * Creates or updates a system config key for the current clinic.
     * Evicts the specific cache entry so the next read fetches the fresh value.
     *
     * Example keys: "SMS_OTP_ENABLED", "EMAIL_OTP_ENABLED"
     * Example values: "true", "false"
     */
    @CacheEvict(value = "systemConfigCache", key = "T(com.dinethdf.ClinicManagementSystem.config.TenantContext).getCurrentTenant() + ':' + #key")
    public void setConfig(String key, String value) {
        Integer clinicId = TenantContext.getCurrentTenant();

        SystemConfig config = repo.findByClinicIdAndKey(clinicId, key)
                .orElse(new SystemConfig());

        config.setClinicId(clinicId);
        config.setKey(key);
        config.setValue(value);
        repo.save(config);
    }
}