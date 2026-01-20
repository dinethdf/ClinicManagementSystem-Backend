package com.dinethdf.ClinicManagementSystem.DTO.Auth;

import lombok.Data;

@Data
public class PasswordChangeRequest {
    private String otp;
    private String newPassword;
    private String confirmPassword;
}