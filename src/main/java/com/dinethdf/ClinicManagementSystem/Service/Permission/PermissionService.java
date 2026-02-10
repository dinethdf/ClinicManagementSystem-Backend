package com.dinethdf.ClinicManagementSystem.Service.Permission;

import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserPrincipal;
import com.dinethdf.ClinicManagementSystem.config.ClinicPlan.SubscriptionPlan;
import com.dinethdf.ClinicManagementSystem.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {
    @Autowired
    EncryptionUtil encryptionUtil;

    public boolean hasFeature(String featureCode) {
        String planCode = getCurrentPlanCode();
        if (planCode == null) return false;
        SubscriptionPlan plan = SubscriptionPlan.fromCode(planCode);
        return plan.hasFeature(featureCode);
    }

    public Integer getCurrentClinicId() {
        return getPrincipal().getClinicId();
    }

    public String getCurrentPlanCode() {
        return encryptionUtil.decryptClinicPlane(getPrincipal().getPlanCode());
    }

//    public String getCurrency() {
//        return getPrincipal().getCurrency();
//    }


    private UserPrincipal getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) auth.getPrincipal();
        }


        throw new RuntimeException("User not authenticated or invalid principal");
    }
}