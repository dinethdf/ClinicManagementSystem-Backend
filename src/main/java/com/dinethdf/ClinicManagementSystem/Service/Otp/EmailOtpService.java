package com.dinethdf.ClinicManagementSystem.Service.Otp;

import com.dinethdf.ClinicManagementSystem.Model.Otp.OtpVerification;
import com.dinethdf.ClinicManagementSystem.Repo.Otp.OtpRepo;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.util.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
public class EmailOtpService {

    @Autowired
    private OtpRepo otpRepository;

    @Autowired
    private EmailService emailService;

    // Constant for Max Attempts
    private static final int MAX_DAILY_ATTEMPTS = 5;

    /**
     * 1. Send OTP (Email Only)
     * - Checks for blocks (Daily limit).
     * - Increments count on request.
     */
    public void sendEmailOtp(String email) {
        Integer clinicId = TenantContext.getCurrentTenant();
        LocalDate today = LocalDate.now();

        // Find or Create Entity
        OtpVerification entity = otpRepository.findByEmailAndClinicId(email, clinicId)
                .orElse(new OtpVerification());

        // --- BLOCKING LOGIC START ---

        // 1. Reset logic: If the last attempt was yesterday (or null), reset count to 0
        if (entity.getLastSmsDate() == null || !entity.getLastSmsDate().isEqual(today)) {
            entity.setDailySmsCount(0);
            entity.setLastSmsDate(today);
        }

        // 2. Block logic: If count >= 5, throw exception
        if (entity.getDailySmsCount() >= MAX_DAILY_ATTEMPTS) {
            throw new ValidationException(
                    "Maximum attempts reached. Please try again after 24 hours.",
                    "ACCOUNT_BLOCKED"
            );
        }

        // --- BLOCKING LOGIC END ---

        // 3. Prepare new OTP
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        entity.setClinicId(clinicId);
        entity.setEmail(email);
        entity.setOtpCode(otp);
        entity.setVerified(false);
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        // Update Activity Tracking
        entity.setLastSmsDate(today);
        entity.setDailySmsCount(entity.getDailySmsCount() + 1); // Increment count for requesting

        // Handle Unique Constraint for Mobile (Dummy Value)
        if (entity.getMobileNo() == null) {
            String dummyMobile = "EM:" + Math.abs(email.hashCode());
            // Ensure length < 20 just in case
            if (dummyMobile.length() > 20) dummyMobile = dummyMobile.substring(0, 20);
            entity.setMobileNo(dummyMobile);
        }

        otpRepository.save(entity);

        // 4. Send Email
        emailService.sendOtpEmail(email, otp);
    }

    /**
     * 2. Verify OTP
     * - Checks for blocks.
     * - Increments count if OTP is WRONG.
     * - DOES NOT reset count here (we reset only on Password Change or Next Day).
     */
    public boolean verifyOtp(String email, String otp) {
        Integer clinicId = TenantContext.getCurrentTenant();
        LocalDate today = LocalDate.now();

        OtpVerification entity = otpRepository.findByEmailAndClinicId(email, clinicId)
                .orElse(null);

        if (entity == null) return false;

        // 1. Check Block (Double check in case they bypass request step)
        if (entity.getLastSmsDate() != null && entity.getLastSmsDate().isEqual(today)) {
            if (entity.getDailySmsCount() >= MAX_DAILY_ATTEMPTS) {
                throw new ValidationException("Maximum attempts reached. Account blocked for 24 hours.", "ACCOUNT_BLOCKED");
            }
        }

        // 2. Check Expiry
        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false; // Expired
        }

        // 3. Verify Code
        if (!entity.getOtpCode().equals(otp)) {
            // WRONG OTP LOGIC:
            // If they enter the wrong code, we punish them by increasing the count.
            // This prevents brute-force guessing.
            entity.setDailySmsCount(entity.getDailySmsCount() + 1);
            entity.setLastSmsDate(today); // Ensure date is updated
            otpRepository.save(entity);

            return false;
        }

        // SUCCESS
        entity.setVerified(true);
        otpRepository.save(entity);
        return true;
    }

    /**
     * 3. Check Verified Status
     */
    public boolean isVerified(String email) {
        Integer clinicId = TenantContext.getCurrentTenant();
        return otpRepository.findByEmailAndClinicId(email, clinicId)
                .map(e -> e.isVerified() && e.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    /**
     * 4. Invalidate (Cleanup)
     * - Called AFTER password is successfully changed.
     * - We RESET the daily count here to allow them to use the system again immediately if needed.
     */
    public void invalidate(String email) {
        Integer clinicId = TenantContext.getCurrentTenant();

        // Find the entity
        otpRepository.findByEmailAndClinicId(email, clinicId).ifPresent(e -> {

            // DIRECT DELETE
            otpRepository.delete(e);

            // Optional: Log this action if you need audit history
            System.out.println("OTP Verified and Record Deleted ");
        });
    }
}