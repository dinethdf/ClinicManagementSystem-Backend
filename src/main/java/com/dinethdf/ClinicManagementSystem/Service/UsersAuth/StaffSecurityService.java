package com.dinethdf.ClinicManagementSystem.Service.UsersAuth;

import com.dinethdf.ClinicManagementSystem.DTO.Auth.PasswordResetDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Auth.SystemAccountStatusDTO;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserAuth;
import com.dinethdf.ClinicManagementSystem.Repo.UsersAuth.UserRepo;
import com.dinethdf.ClinicManagementSystem.Service.Otp.EmailOtpService;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffSecurityService {

    @Autowired private UserRepo userRepo;
    @Autowired private EmailOtpService emailOtpService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // ==========================================
    // A. SELF-SERVICE FLOWS
    // ==========================================

    public void initiateForgotPassword(String email) {
        Integer clinicId = TenantContext.getCurrentTenant();
        UserAuth user = userRepo.findByEmailAndClinicId(email, clinicId);
        if (user == null) throw new ValidationException("Email not found", "USER_NOT_FOUND");

        emailOtpService.sendEmailOtp(email);
    }

    @Transactional
    public void completeForgotPassword(PasswordResetDTO.CompleteRequest request) {
        Integer clinicId = TenantContext.getCurrentTenant();
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Passwords do not match", "PASSWORD_MISMATCH");
        }
        if (!emailOtpService.verifyOtp(request.getEmail(), request.getOtp())) {
            throw new ValidationException("Invalid or Expired OTP", "INVALID_OTP");
        }

        UserAuth user = userRepo.findByEmailAndClinicId(request.getEmail(), clinicId);
        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepo.save(user);
        emailOtpService.invalidate(request.getEmail());
    }

    public void requestChangePasswordOtp() {
        String email = getCurrentUserEmail();
        initiateForgotPassword(email);
    }

    @Transactional
    public void changeOwnPassword(String otp, String newPassword, String confirmPassword) {
        PasswordResetDTO.CompleteRequest request = new PasswordResetDTO.CompleteRequest();
        request.setEmail(getCurrentUserEmail());
        request.setOtp(otp);
        request.setNewPassword(newPassword);
        request.setConfirmPassword(confirmPassword);

        completeForgotPassword(request);
    }

    // ==========================================
    // B. ADMIN MANAGEMENT FLOWS
    // ==========================================

    public SystemAccountStatusDTO getAccountStatus(Long detailsId) {
        Integer clinicId = TenantContext.getCurrentTenant();
        UserAuth user = userRepo.findByDetailsIdAndClinicId(detailsId, clinicId);

        if (user == null) {
            return new SystemAccountStatusDTO(false, null, false, false);
        }

        // 🚨 Note: 2FA is hardcoded to false here because your DB entity doesn't have it yet!
        // Also: isSuspended is the opposite of getEnable()
        boolean isSuspended = user.getEnable() == null || !user.getEnable();

        return new SystemAccountStatusDTO(true, user.getEmail(), false, isSuspended);
    }

    @Transactional
    public void manualPasswordReset(Long detailsId, String newPassword) {
        Integer clinicId = TenantContext.getCurrentTenant();
        UserAuth user = userRepo.findByDetailsIdAndClinicId(detailsId, clinicId);
        if (user == null) throw new ValidationException("Staff account not found", "NOT_FOUND");

        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);
    }

    @Transactional
    public void toggleSuspension(Long detailsId, boolean suspend) {
        Integer clinicId = TenantContext.getCurrentTenant();
        UserAuth user = userRepo.findByDetailsIdAndClinicId(detailsId, clinicId);
        if (user == null) throw new ValidationException("Staff account not found", "NOT_FOUND");

        // 🚨 If suspend is true, isEnable becomes false.
        user.setEnable(!suspend);
        userRepo.save(user);
    }

    @Transactional
    public void disableTwoFactor(Long detailsId) {
        // You can leave this empty for now until you add `isTwoFactorEnabled`
        // to your UserAuth entity in the future.
    }

    public void sendResetLink(Long detailsId) {
        Integer clinicId = TenantContext.getCurrentTenant();
        UserAuth user = userRepo.findByDetailsIdAndClinicId(detailsId, clinicId);
        if (user != null) {
            // Trigger email service here
        }
    }
}