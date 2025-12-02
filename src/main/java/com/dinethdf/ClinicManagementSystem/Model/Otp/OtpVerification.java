package com.dinethdf.ClinicManagementSystem.Model.Otp;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "otpVerification") // Matches DB table
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String mobileNo;

    private String email;

    @Column(nullable = false)
    private String otpCode;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private boolean verified = false;

    // Abuse Prevention
    private int dailySmsCount = 0;
    private LocalDate lastSmsDate;

    // Audit Fields
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime updatedDate;
    private String updatedBy;

    // Auto-fill created fields
    @PrePersist
    protected void onCreate() {
        if (createdDate == null) createdDate = LocalDateTime.now();
        if (createdBy == null) createdBy = "SYSTEM";
    }

    // Getters and Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMobileNo() { return mobileNo; }
    public void setMobileNo(String mobileNo) { this.mobileNo = mobileNo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public int getDailySmsCount() { return dailySmsCount; }
    public void setDailySmsCount(int dailySmsCount) { this.dailySmsCount = dailySmsCount; }

    public LocalDate getLastSmsDate() { return lastSmsDate; }
    public void setLastSmsDate(LocalDate lastSmsDate) { this.lastSmsDate = lastSmsDate; }

}