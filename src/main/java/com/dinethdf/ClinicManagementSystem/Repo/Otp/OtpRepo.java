package com.dinethdf.ClinicManagementSystem.Repo.Otp;

import com.dinethdf.ClinicManagementSystem.Model.Otp.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpRepo extends JpaRepository<OtpVerification, Long> {
    // Scoped to Clinic
    Optional<OtpVerification> findByMobileNoAndClinicId(String mobileNo, Integer clinicId);

    Optional<OtpVerification> findByEmailAndClinicId(String email, Integer clinicId);
}