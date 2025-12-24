package com.dinethdf.ClinicManagementSystem.Controller.Otp;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Otp.OtpSendRequestDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Otp.OtpVerifyRequestDTO;
import com.dinethdf.ClinicManagementSystem.Service.Otp.OtpService;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    @Autowired
    private OtpService otpService;

    @PostMapping("/send")
    public ApiResponse<String> sendOtp(@RequestBody OtpSendRequestDTO request) {

        String mobileNo = request.getMobileNo();
        String email = request.getEmail();
        Integer clinicId = request.getClinicId();

        // 1. Validation
        if (mobileNo == null || mobileNo.trim().isEmpty()) {
            return ApiResponse.error("Mobile number is required");
        }

        if (clinicId == null || clinicId == 0) {
            return ApiResponse.error("Clinic ID is missing or invalid");
        }

        // 2. Set Context & Execute safely
        try {
            TenantContext.setCurrentTenant(clinicId);
            System.out.println("Processing OTP Send for Clinic ID: " + clinicId);

            String resultMessage = otpService.sendOtp(mobileNo, email);
            return ApiResponse.success("OTP Processed", resultMessage);

        } finally {
            // 3. CRITICAL: Always clear the context
            TenantContext.clear();
        }
    }

    @PostMapping("/verify")
    public ApiResponse<Boolean> verifyOtp(@RequestBody OtpVerifyRequestDTO request) {

        // We MUST have clinicId here too, or we can't find the user in the DB
        Integer clinicId = request.getClinicId();

        if (clinicId == null || clinicId == 0) {
            return ApiResponse.error("Clinic ID is missing for verification");
        }

        try {
            // 1. Set Context
            TenantContext.setCurrentTenant(clinicId);

            // 2. Verify
            boolean verified = otpService.verifyOtp(request.getMobileNo(), request.getOtp());

            if (verified) {
                return ApiResponse.success(true, "Verified successfully.");
            } else {
                return ApiResponse.error("Invalid OTP or expired.");
            }
        } finally {
            // 3. Clear Context
            TenantContext.clear();
        }
    }
}