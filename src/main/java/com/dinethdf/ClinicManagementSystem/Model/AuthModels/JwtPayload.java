package com.dinethdf.ClinicManagementSystem.Model.AuthModels;

import com.dinethdf.ClinicManagementSystem.Model.Clinic.ClinicSettings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtPayload implements Serializable {

    private String sub;  // User email


    private Integer clinicId;    // INT (1, 2, 3...)
    private String clinicName;
    private String planCode;     // "PLAN_PRO" (plain text)

    private ClinicSettings settings;

    // Expiry
    private Long exp;
}