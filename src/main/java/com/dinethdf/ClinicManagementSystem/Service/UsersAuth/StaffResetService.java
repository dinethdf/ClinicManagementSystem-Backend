package com.dinethdf.ClinicManagementSystem.Service.UsersAuth;

import com.dinethdf.ClinicManagementSystem.DTO.Auth.PasswordResetDTO;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserAuth;
import com.dinethdf.ClinicManagementSystem.Repo.UsersAuth.UserRepo;
import com.dinethdf.ClinicManagementSystem.Service.Otp.EmailOtpService;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffResetService {

    @Autowired private UserRepo userRepo;
    @Autowired private EmailOtpService emailOtpService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    // 1. Send OTP (Public Access)
    public void initiateReset(String email) {
        Integer clinicId = TenantContext.getCurrentTenant();

        // Check if Staff exists
        UserAuth user = userRepo.findByEmailAndClinicId(email, clinicId);
        if (user == null) {
            // Security Best Practice: You might want to return "Success" even if email fails
            // to prevent user enumeration, but for internal apps, specific error is fine.
            throw new ValidationException("Email not found in Staff records", "USER_NOT_FOUND");
        }

        emailOtpService.sendEmailOtp(email);
    }

    // 2. Complete Reset (Verify OTP + Update Password)
    @Transactional
    public void completeReset(PasswordResetDTO.CompleteRequest request) {
        Integer clinicId = TenantContext.getCurrentTenant();

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Passwords do not match", "PASSWORD_MISMATCH");
        }

        // Verify OTP directly here
        if (!emailOtpService.verifyOtp(request.getEmail(), request.getOtp())) {
            throw new ValidationException("Invalid or Expired OTP", "INVALID_OTP");
        }

        UserAuth user = userRepo.findByEmailAndClinicId(request.getEmail(), clinicId);
        if (user == null) throw new ValidationException("User not found", "USER_NOT_FOUND");

        // Update Password
        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepo.save(user);

        // Clean up OTP
        emailOtpService.invalidate(request.getEmail());
    }
}