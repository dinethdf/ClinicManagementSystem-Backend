package com.dinethdf.ClinicManagementSystem.Controller.UsersAuth;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Auth.PasswordResetDTO;

import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.PatientResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/patient/forgot-password")
public class PatientResetController {

    @Autowired private PatientResetService resetService;

    @PostMapping("/init")
    public ResponseEntity<ApiResponse<String>> initiateReset(@RequestBody PasswordResetDTO.InitRequest request) {
        resetService.initiateReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("OTP sent to email", "OTP_SENT"));
    }

    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<String>> completeReset(@RequestBody PasswordResetDTO.CompleteRequest request) {
        resetService.completeReset(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", "SUCCESS"));
    }
}