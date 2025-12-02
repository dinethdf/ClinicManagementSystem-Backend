package com.dinethdf.ClinicManagementSystem.Service.SystemConfig;

import com.dinethdf.ClinicManagementSystem.Model.SystemConfig.SystemConfig;
import com.dinethdf.ClinicManagementSystem.Repo.SystemConfig.SystemConfigRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {

    @Autowired
    private SystemConfigRepo repo;

    @PostConstruct
    public void init() {
        if (!repo.findByKey("SMS_OTP_ENABLED").isPresent()) {
            repo.save(new SystemConfig("SMS_OTP_ENABLED", "true"));
        }
        if (!repo.findByKey("EMAIL_OTP_ENABLED").isPresent()) {
            repo.save(new SystemConfig("EMAIL_OTP_ENABLED", "true"));
        }
    }

    public boolean isSmsEnabled() {
        return Boolean.parseBoolean(repo.findByKey("SMS_OTP_ENABLED").map(SystemConfig::getValue).orElse("false"));
    }

    public boolean isEmailEnabled() {
        return Boolean.parseBoolean(repo.findByKey("EMAIL_OTP_ENABLED").map(SystemConfig::getValue).orElse("false"));
    }
}