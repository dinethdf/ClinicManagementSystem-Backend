package com.dinethdf.ClinicManagementSystem.Controller.UsersAuth;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Auth.PasswordChangeRequest;
import com.dinethdf.ClinicManagementSystem.DTO.Auth.PasswordResetDTO;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.StaffSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/staff")
public class StaffAuthSecurityController {

    @Autowired private StaffSecurityService securityService;

    // --- FORGOT PASSWORD (PUBLIC) ---
    @PostMapping("/forgot-password/init")
    public ResponseEntity<ApiResponse<String>> initiateReset(@RequestBody PasswordResetDTO.InitRequest request) {
        securityService.initiateForgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("OTP sent to email", "OTP_SENT"));
    }

    @PostMapping("/forgot-password/complete")
    public ResponseEntity<ApiResponse<String>> completeReset(@RequestBody PasswordResetDTO.CompleteRequest request) {
        securityService.completeForgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", "SUCCESS"));
    }

    // --- CHANGE PASSWORD (AUTHENTICATED) ---
    @PostMapping("/password/request-otp")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<ApiResponse<String>> requestOtp() {
        securityService.requestChangePasswordOtp();
        return ResponseEntity.ok(ApiResponse.success("OTP sent to your email", "OTP_SENT"));
    }

    @PostMapping("/password/change")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<ApiResponse<String>> changePassword(@RequestBody PasswordChangeRequest dto) {
        securityService.changeOwnPassword(dto.getOtp(), dto.getNewPassword(), dto.getConfirmPassword());
        return ResponseEntity.ok(ApiResponse.success("Password updated successfully", "SUCCESS"));
    }
}