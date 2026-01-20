package com.dinethdf.ClinicManagementSystem.Controller.UsersAuth;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Auth.PasswordChangeRequest;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.PatientPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/patient/password")
public class PatientPasswordController {

    @Autowired private PatientPasswordService patientService;

    @PostMapping("/request-otp")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ApiResponse<String>> requestOtp() {
        patientService.requestOtp();
        return ResponseEntity.ok(ApiResponse.success("OTP sent to your email", "OTP_SENT"));
    }

    @PostMapping("/change")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ApiResponse<String>> changePassword(@RequestBody PasswordChangeRequest dto) {
        patientService.changePassword(dto.getOtp(), dto.getNewPassword(), dto.getConfirmPassword());
        return ResponseEntity.ok(ApiResponse.success("Password updated successfully", "SUCCESS"));
    }
}