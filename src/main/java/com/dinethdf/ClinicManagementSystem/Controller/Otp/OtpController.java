package com.dinethdf.ClinicManagementSystem.Controller.Otp;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.Service.Otp.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    @Autowired
    private OtpService otpService;

    @PostMapping("/send")
    public ApiResponse<String> sendOtp(@RequestParam String mobileNo) {
        otpService.sendOtp(mobileNo);
        return ApiResponse.success("OTP sent successfully.", "OTP has been sent to " + mobileNo);
    }


    @PostMapping("/verify")
    public ApiResponse<Boolean> verifyOtp(@RequestParam String mobileNo,
                                          @RequestParam String otp) {
        boolean verified = otpService.verifyOtp(mobileNo, otp);

        if (verified) {
            return ApiResponse.success(true, "Mobile number verified successfully.");
        } else {
            return ApiResponse.error("Invalid OTP or OTP expired.");
        }
    }
}

