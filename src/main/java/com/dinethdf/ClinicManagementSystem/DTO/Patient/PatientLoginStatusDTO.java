package com.dinethdf.ClinicManagementSystem.DTO.Patient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientLoginStatusDTO {
    private boolean hasLogin;
    private boolean emailOnFile;
    private String loginStatus; // "ACTIVE" or "DISABLED"
}