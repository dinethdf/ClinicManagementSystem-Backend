package com.dinethdf.ClinicManagementSystem.Service.Otp;

import com.dinethdf.ClinicManagementSystem.Model.Otp.OtpVerification;
import com.dinethdf.ClinicManagementSystem.Repo.Otp.OtpRepo;
import com.dinethdf.ClinicManagementSystem.util.MobileFormatUtil;
import com.dinethdf.ClinicManagementSystem.util.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OtpService {

    @Autowired
    private OtpRepo otpRepository;

    @Autowired
    private SmsService smsService;

    @Autowired
    private MobileFormatUtil mobileFormatUtil;

    // Send new OTP
    public void sendOtp(String rawMobile) {

        String mobile = mobileFormatUtil.formatToLankaInternational(rawMobile);

        String otp = generateOtp();

        OtpVerification entity = otpRepository.findByMobileNo(mobile)
                .orElse(new OtpVerification());

        entity.setMobileNo(mobile);
        entity.setOtpCode(otp);
        entity.setVerified(false);
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(3));

        otpRepository.save(entity);

        String message = "CureGuard Verification Code: " + otp;
        smsService.sendPaymentSms(mobile, message);

        System.out.println("Sent OTP to " + mobile + ": " + otp);
    }

    // Verify OTP
    public boolean verifyOtp(String rawMobile, String otp) {

        String mobile = mobileFormatUtil.formatToLankaInternational(rawMobile);

        OtpVerification entity = otpRepository.findByMobileNo(mobile)
                .orElse(null);

        if (entity == null) return false;
        if (entity.isVerified()) return true;
        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) return false;

        if (!entity.getOtpCode().equals(otp)) return false;

        entity.setVerified(true);
        otpRepository.save(entity);

        return true;
    }

    // Check if verified
    public boolean isVerified(String rawMobile) {

        String mobile = mobileFormatUtil.formatToLankaInternational(rawMobile);

        OtpVerification entity = otpRepository.findByMobileNo(mobile)
                .orElse(null);

        return entity != null && entity.isVerified();
    }

    private String generateOtp() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
}

