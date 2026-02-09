package com.dinethdf.ClinicManagementSystem.DTO.Clinic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicFeatureDTO implements Serializable {
    private String featureCode;   // e.g., "FEAT_BILLING"
    private String featureName;   // e.g., "Billing & Invoicing"
    private String category;      // e.g., "CORE"
    private boolean enabled;      // TRUE/FALSE (Calculated from DB View)
}