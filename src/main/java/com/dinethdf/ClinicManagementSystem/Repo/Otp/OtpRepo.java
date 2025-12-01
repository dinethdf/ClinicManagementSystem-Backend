package com.dinethdf.ClinicManagementSystem.Repo.Otp;

import com.dinethdf.ClinicManagementSystem.Model.Otp.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpRepo extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByMobileNo(String mobileNo);

}

