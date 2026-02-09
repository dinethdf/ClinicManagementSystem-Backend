package com.dinethdf.ClinicManagementSystem.Service.SystemConfig;

import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicCompleteContextDTO;
import com.dinethdf.ClinicManagementSystem.Model.SystemConfig.SystemConfig;
import com.dinethdf.ClinicManagementSystem.Repo.SystemConfig.SystemConfigRepo;
import com.dinethdf.ClinicManagementSystem.Service.Clinic.ClinicServiceImpl;
import com.dinethdf.ClinicManagementSystem.config.TenantContext; // <--- Import Context
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final ClinicServiceImpl clinicService;

    public boolean isSmsEnabled() {

        ClinicCompleteContextDTO context = clinicService.getFullClinicContext();
        boolean featureAllowed = context.hasFeature("FEAT_SMS_NOTIFICATIONS");

//        boolean settingEnabled = context.getClinicInfo().get(); // Assuming you mapped this in DTO

//        return featureAllowed && settingEnabled;
        return featureAllowed;
    }

    public boolean isEmailEnabled() {

        ClinicCompleteContextDTO context = clinicService.getFullClinicContext();
        boolean featureAllowed = context.hasFeature("FEAT_EMAIL_NOTIFICATIONS");

//        boolean settingEnabled = context.getClinicInfo().get(); // Assuming you mapped this in DTO

//        return featureAllowed && settingEnabled;
        return featureAllowed;
    }

}
