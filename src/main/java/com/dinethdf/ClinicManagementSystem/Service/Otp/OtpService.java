package com.dinethdf.ClinicManagementSystem.Service.Otp;

import com.dinethdf.ClinicManagementSystem.Model.Otp.OtpVerification;
import com.dinethdf.ClinicManagementSystem.Repo.Otp.OtpRepo;
import com.dinethdf.ClinicManagementSystem.Service.SystemConfig.SystemConfigService;
import com.dinethdf.ClinicManagementSystem.config.TenantContext; // <--- Import Context
import com.dinethdf.ClinicManagementSystem.util.EmailService;
import com.dinethdf.ClinicManagementSystem.util.MobileFormatUtil;
import com.dinethdf.ClinicManagementSystem.util.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
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

        Integer clinicId = TenantContext.getCurrentTenant();

        // Find existing record for THIS clinic
        OtpVerification entity = otpRepository.findByMobileNoAndClinicId(mobile, clinicId)
                .orElse(new OtpVerification());

        // 2. Daily Limit Logic (Reset count if new day)
        if (entity.getLastSmsDate() == null || !entity.getLastSmsDate().isEqual(today)) {
            entity.setDailySmsCount(0);
            entity.setLastSmsDate(today);
        }

        // 3. Prevent spamming (Check if active OTP exists)
        if (entity.getOtpCode() != null
                && entity.getExpiresAt().isAfter(LocalDateTime.now())
                && !entity.isVerified()) {
            // Masked OTP for security in response
            return "Active OTP already exists. Please check your inbox for code: " + entity.getOtpCode().substring(0, 2) + "****";
        }

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        // 4. Update/Set Entity Details
        entity.setClinicId(clinicId); // Ensure Clinic ID is set
        entity.setMobileNo(mobile);
        entity.setEmail(email);
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

        if (smsEnabled && entity.getDailySmsCount() >= 2) {
            return "Daily SMS limit reached. Please try again tomorrow or contact support.";
        }

        return "OTP processing failed (Check system config).";
    }

    public boolean verifyOtp(String rawMobile, String otp) {
        String mobile = mobileFormatUtil.formatToLankaInternational(rawMobile);

        // Find by Mobile AND Clinic ID (from Context)
        OtpVerification entity = otpRepository.findByMobileNoAndClinicId(
                mobile, TenantContext.getCurrentTenant()
        ).orElse(null);

        if (entity == null) return false;
        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) return false;
        if (!entity.getOtpCode().equals(otp)) return false;

        entity.setVerified(true);
        otpRepository.save(entity);
        return true;
    }

    // Check if verified AND not expired
    public boolean isVerified(String rawMobile) {
        String mobile = mobileFormatUtil.formatToLankaInternational(rawMobile);

        OtpVerification entity = otpRepository.findByMobileNoAndClinicId(
                mobile, TenantContext.getCurrentTenant()
        ).orElse(null);

        if (entity == null) return false;

        return entity.isVerified() && entity.getExpiresAt().isAfter(LocalDateTime.now());
    }

    // Security: Reset verification after successful action
    public void invalidateOtp(String rawMobile) {
        String mobile = mobileFormatUtil.formatToLankaInternational(rawMobile);

        otpRepository.findByMobileNoAndClinicId(
                mobile, TenantContext.getCurrentTenant()
        ).ifPresent(entity -> {
            entity.setVerified(false);
            entity.setExpiresAt(LocalDateTime.now());
            otpRepository.save(entity);
        });
    }
}