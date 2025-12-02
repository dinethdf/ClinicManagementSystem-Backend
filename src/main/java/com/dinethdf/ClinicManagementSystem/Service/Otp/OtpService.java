package com.dinethdf.ClinicManagementSystem.Service.Otp;

import com.dinethdf.ClinicManagementSystem.Model.Otp.OtpVerification;
import com.dinethdf.ClinicManagementSystem.Repo.Otp.OtpRepo;
import com.dinethdf.ClinicManagementSystem.Service.SystemConfig.SystemConfigService;
import com.dinethdf.ClinicManagementSystem.util.EmailService;
import com.dinethdf.ClinicManagementSystem.util.MobileFormatUtil;
import com.dinethdf.ClinicManagementSystem.util.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class OtpService {

    @Autowired private OtpRepo otpRepository;
    @Autowired private SmsService smsService;
    @Autowired private EmailService emailService;
    @Autowired private MobileFormatUtil mobileFormatUtil;
    @Autowired private SystemConfigService configService;

    // Send OTP (Handles SMS limit + Email + Config)
    public String sendOtp(String rawMobile, String email) {

        String mobile = mobileFormatUtil.formatToLankaInternational(rawMobile);
        boolean smsEnabled = configService.isSmsEnabled();
        boolean emailEnabled = configService.isEmailEnabled();
        LocalDate today = LocalDate.now();

        OtpVerification entity = otpRepository.findByMobileNo(mobile)
                .orElse(new OtpVerification());

        // 2. Daily Limit Logic (Reset count if new day)
        if (entity.getLastSmsDate() == null || !entity.getLastSmsDate().isEqual(today)) {
            entity.setDailySmsCount(0);
            entity.setLastSmsDate(today);
        }

        if (entity.getOtpCode() != null
                && entity.getExpiresAt().isAfter(LocalDateTime.now())
                && !entity.isVerified()) {

            return "Active OTP already exists. Please check your inbox for code: " + entity.getOtpCode().substring(0, 2) + "****";
        }

        // 4. Generate New OTP (Since old one is expired or verified)
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        entity.setMobileNo(mobile);
        entity.setEmail(email); // Update email
        entity.setOtpCode(otp);
        entity.setVerified(false);
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(3));

        // 5. Send via SMS (If Enabled AND Limit < 2)
        boolean smsSent = false;
        boolean reachMaxSms = false;
        if (smsEnabled) {
            if (entity.getDailySmsCount() < 2) {
                String message = "CureGuard Verification Code: " + otp;
                boolean success = smsService.sendPaymentSms(mobile, message);

                if (success) {
                    entity.setDailySmsCount(entity.getDailySmsCount() + 1);
                    smsSent = true;
                }
            } else {
                System.out.println("Daily SMS limit reached for " + mobile);
                // We do NOT throw an error, we just fallback to Email or nothing.
                reachMaxSms = true;
            }
        }

        // 6. Send via Email (If Enabled & Email Provided)
        boolean emailSent = false;

        if (emailEnabled && email != null && !email.isEmpty()) {
            emailService.sendOtpEmail(email, otp);
            emailSent = true;
        }

        otpRepository.save(entity);

        // 7. Construct Response Message
        if (smsSent && emailSent) return "OTP sent to Mobile and Email.";
        if (smsSent) return "OTP sent to Mobile.";
        if (emailSent && reachMaxSms) return "Unable to send SMS (limit reached). OTP sent to your email instead.";
        if (emailSent) return "OTP sent to Email.";

        // If limit reached and no email provided
        if (smsEnabled && entity.getDailySmsCount() >= 2) {
            return "Daily SMS limit reached. Please try again tomorrow or contact support.";
        }

        return "OTP processing failed (Check system config).";
    }

    // Verify OTP (Same as before, simplified)
    public boolean verifyOtp(String rawMobile, String otp) {
        String mobile = mobileFormatUtil.formatToLankaInternational(rawMobile);
        OtpVerification entity = otpRepository.findByMobileNo(mobile).orElse(null);

        if (entity == null) return false;
        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) return false;
        if (!entity.getOtpCode().equals(otp)) return false;

        entity.setVerified(true);
        otpRepository.save(entity);
        return true;
    }

// In com.dinethdf.ClinicManagementSystem.Service.Otp.OtpService

    // Check if verified AND not expired
    public boolean isVerified(String rawMobile) {
        String mobile = mobileFormatUtil.formatToLankaInternational(rawMobile);

        OtpVerification entity = otpRepository.findByMobileNo(mobile).orElse(null);

        if (entity == null) return false;

        // Must be marked verified AND expiry time must be in the future
        // (Optional: You can remove the expiry check if verification should last forever until used)
        return entity.isVerified() && entity.getExpiresAt().isAfter(LocalDateTime.now());
    }

    // Security: Reset verification after successful action
    public void invalidateOtp(String rawMobile) {
        String mobile = mobileFormatUtil.formatToLankaInternational(rawMobile);

        otpRepository.findByMobileNo(mobile).ifPresent(entity -> {
            entity.setVerified(false); // Reset status
            entity.setExpiresAt(LocalDateTime.now()); // Expire it immediately
            otpRepository.save(entity);
        });
    }

}