package com.dinethdf.ClinicManagementSystem.Service.UsersAuth;

 // Ensure this Repo exists
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.PatientAuth; // Ensure this Model exists
import com.dinethdf.ClinicManagementSystem.Repo.UsersAuth.PatientRepo;
import com.dinethdf.ClinicManagementSystem.Service.Otp.EmailOtpService;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientPasswordService {

    @Autowired private PatientRepo patientRepo;
    @Autowired private EmailOtpService emailOtpService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    private String getCurrentPatientEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public void requestOtp() {
        String email = getCurrentPatientEmail();
        Integer clinicId = TenantContext.getCurrentTenant();

        // Validate user exists in Patient table
        if (patientRepo.findByEmailAndClinicId(email, clinicId) == null) {
            throw new ValidationException("Patient account not found", "PATIENT_NOT_FOUND");
        }

        emailOtpService.sendEmailOtp(email);
    }

    @Transactional
    public void changePassword(String otp, String newPassword, String confirmPassword) {
        String email = getCurrentPatientEmail();
        Integer clinicId = TenantContext.getCurrentTenant();

        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match", "PASSWORD_MISMATCH");
        }

        if (!emailOtpService.isVerified(email)) {
            if (!emailOtpService.verifyOtp(email, otp)) {
                throw new ValidationException("Invalid or Expired OTP", "INVALID_OTP");
            }
        }

        // Update Patient Password
        PatientAuth patient = patientRepo.findByEmailAndClinicId(email, clinicId);
        if (patient == null) throw new ValidationException("Patient not found", "PATIENT_NOT_FOUND");

        patient.setPassword(encoder.encode(newPassword));
        patientRepo.save(patient);

        emailOtpService.invalidate(email);
    }
}