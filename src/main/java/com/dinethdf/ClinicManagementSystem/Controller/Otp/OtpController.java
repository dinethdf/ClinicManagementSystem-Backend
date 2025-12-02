package com.dinethdf.ClinicManagementSystem.Controller.Otp;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Otp.OtpSendRequestDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Otp.OtpVerifyRequestDTO;
import com.dinethdf.ClinicManagementSystem.Service.Otp.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    @Autowired
    private OtpService otpService;

    @PostMapping("/send")
    public ApiResponse<String> sendOtp(@RequestBody OtpSendRequestDTO request) {

        // Extract values from the Request Body
        String mobileNo = request.getMobileNo();
        String email = request.getEmail();

        // Validation check for empty mobile
        if(mobileNo == null || mobileNo.trim().isEmpty()){
            return ApiResponse.error("Mobile number is required");
        }

        // Call Service
        String resultMessage = otpService.sendOtp(mobileNo, email);

        return ApiResponse.success("OTP Processed", resultMessage);
    }

    @PostMapping("/verify")
    public ApiResponse<Boolean> verifyOtp(@RequestBody OtpVerifyRequestDTO request) {

        // Extract values from the Request Body
        boolean verified = otpService.verifyOtp(request.getMobileNo(), request.getOtp());

        if (verified) {
            return ApiResponse.success(true, "Verified successfully.");
        } else {
            return ApiResponse.error("Invalid OTP or expired.");
        }
    }
}