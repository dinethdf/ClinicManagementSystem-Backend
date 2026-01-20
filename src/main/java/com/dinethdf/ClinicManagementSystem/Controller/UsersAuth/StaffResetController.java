package com.dinethdf.ClinicManagementSystem.Controller.UsersAuth;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Auth.PasswordResetDTO;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.StaffResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/staff/forgot-password")
public class StaffResetController {

    @Autowired private StaffResetService resetService;

    // Step 1: Request Code
    @PostMapping("/init")
    public ResponseEntity<ApiResponse<String>> initiateReset(@RequestBody PasswordResetDTO.InitRequest request) {
        resetService.initiateReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("OTP sent to email", "OTP_SENT"));
    }

    // Step 2: Submit Code & New Password
    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<String>> completeReset(@RequestBody PasswordResetDTO.CompleteRequest request) {
        resetService.completeReset(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", "SUCCESS"));
    }
}