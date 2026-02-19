package com.dinethdf.ClinicManagementSystem.DTO;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private String loginType; // "USER" or "PATIENT"
    private Integer clinicId;  // Option A: Direct ID
    private String subdomain;  // Option B: Subdomain String
}
