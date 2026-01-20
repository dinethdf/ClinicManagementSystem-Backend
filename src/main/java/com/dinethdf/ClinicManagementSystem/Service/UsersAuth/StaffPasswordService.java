package com.dinethdf.ClinicManagementSystem.Service.UsersAuth;

import com.dinethdf.ClinicManagementSystem.Repo.UsersAuth.UserRepo;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserAuth;
import com.dinethdf.ClinicManagementSystem.Service.Otp.EmailOtpService;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffPasswordService {

    @Autowired private UserRepo userRepo;
    @Autowired private EmailOtpService emailOtpService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    private String getCurrentStaffEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public void requestOtp() {
        String email = getCurrentStaffEmail();
        Integer clinicId = TenantContext.getCurrentTenant();

        // Validate user exists in Staff table
        if (userRepo.findByEmailAndClinicId(email, clinicId) == null) {
            throw new ValidationException("Staff user not found", "USER_NOT_FOUND");
        }

        emailOtpService.sendEmailOtp(email);
    }

    @Transactional
    public void changePassword(String otp, String newPassword, String confirmPassword) {
        String email = getCurrentStaffEmail();
        Integer clinicId = TenantContext.getCurrentTenant();

        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match", "PASSWORD_MISMATCH");
        }

        // Check if OTP was verified
        if (!emailOtpService.isVerified(email)) {
            // Attempt to verify right now
            if (!emailOtpService.verifyOtp(email, otp)) {
                throw new ValidationException("Invalid or Expired OTP", "INVALID_OTP");
            }
        }

        // Update Password
        UserAuth user = userRepo.findByEmailAndClinicId(email, clinicId);
        if (user == null) throw new ValidationException("User not found", "USER_NOT_FOUND");

        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);

        emailOtpService.invalidate(email);
    }
}