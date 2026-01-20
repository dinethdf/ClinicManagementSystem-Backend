package com.dinethdf.ClinicManagementSystem.Controller.UsersAuth;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Auth.PasswordChangeRequest;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.StaffPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/staff/password")
public class StaffPasswordController {

    @Autowired private StaffPasswordService staffService;

    @PostMapping("/request-otp")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<ApiResponse<String>> requestOtp() {
        staffService.requestOtp();
        return ResponseEntity.ok(ApiResponse.success("OTP sent to your email", "OTP_SENT"));
    }

    @PostMapping("/change")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<ApiResponse<String>> changePassword(@RequestBody PasswordChangeRequest dto) {
        staffService.changePassword(dto.getOtp(), dto.getNewPassword(), dto.getConfirmPassword());
        return ResponseEntity.ok(ApiResponse.success("Password updated successfully", "SUCCESS"));
    }
}