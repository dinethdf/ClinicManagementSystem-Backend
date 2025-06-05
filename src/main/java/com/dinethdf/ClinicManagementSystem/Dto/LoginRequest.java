package com.dinethdf.ClinicManagementSystem.Dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private String loginType; // "USER" or "PATIENT"
}
