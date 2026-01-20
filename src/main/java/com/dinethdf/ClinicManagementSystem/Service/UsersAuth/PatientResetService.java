package com.dinethdf.ClinicManagementSystem.Service.UsersAuth;

import com.dinethdf.ClinicManagementSystem.DTO.Auth.PasswordResetDTO;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.PatientAuth;
import com.dinethdf.ClinicManagementSystem.Repo.UsersAuth.PatientRepo;
import com.dinethdf.ClinicManagementSystem.Service.Otp.EmailOtpService;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientResetService {

    @Autowired private PatientRepo patientRepo;
    @Autowired private EmailOtpService emailOtpService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public void initiateReset(String email) {
        Integer clinicId = TenantContext.getCurrentTenant();

        // Check if Patient exists
        PatientAuth patient = patientRepo.findByEmailAndClinicId(email, clinicId);
        if (patient == null) {
            throw new ValidationException("Email not found in Patient records", "PATIENT_NOT_FOUND");
        }

        emailOtpService.sendEmailOtp(email);
    }

    @Transactional
    public void completeReset(PasswordResetDTO.CompleteRequest request) {
        Integer clinicId = TenantContext.getCurrentTenant();

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Passwords do not match", "PASSWORD_MISMATCH");
        }

        if (!emailOtpService.verifyOtp(request.getEmail(), request.getOtp())) {
            throw new ValidationException("Invalid or Expired OTP", "INVALID_OTP");
        }

        PatientAuth patient = patientRepo.findByEmailAndClinicId(request.getEmail(), clinicId);
        if (patient == null) throw new ValidationException("Patient not found", "PATIENT_NOT_FOUND");

        patient.setPassword(encoder.encode(request.getNewPassword()));
        patientRepo.save(patient);

        emailOtpService.invalidate(request.getEmail());
    }
}