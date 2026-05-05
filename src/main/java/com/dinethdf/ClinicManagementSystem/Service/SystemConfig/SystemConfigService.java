package com.dinethdf.ClinicManagementSystem.Service.SystemConfig;

import com.dinethdf.ClinicManagementSystem.Repo.SystemConfig.SystemConfigRepo;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {

    @Autowired
    private SystemConfigRepo repo;

    /* NOTE: We cannot auto-initialize defaults at startup (@PostConstruct)
     because we don't know which Clinic ID to use.
     Defaults are handled dynamically in the getters below.
    */
    public boolean isSmsEnabled() {
        Integer clinicId = TenantContext.getCurrentTenant();
//        return repo.findByClinicIdAndKey(clinicId, "SMS_OTP_ENABLED")
//                .map(config -> Boolean.parseBoolean(config.getValue()))
//                .orElse(true);
         return true;
    }

    public boolean isEmailEnabled() {
        Integer clinicId = TenantContext.getCurrentTenant();
//        return repo.findByClinicIdAndKey(clinicId, "EMAIL_OTP_ENABLED")
//                .map(config -> Boolean.parseBoolean(config.getValue()))
//                .orElse(true);
        return true;
    }
}