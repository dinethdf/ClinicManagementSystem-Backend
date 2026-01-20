package com.dinethdf.ClinicManagementSystem.DTO.Auth;

import lombok.Data;

public class PasswordResetDTO {

    @Data
    public static class InitRequest {
        private String email;
    }

    @Data
    public static class CompleteRequest {
        private String email;
        private String otp;
        private String newPassword;
        private String confirmPassword;
    }
}