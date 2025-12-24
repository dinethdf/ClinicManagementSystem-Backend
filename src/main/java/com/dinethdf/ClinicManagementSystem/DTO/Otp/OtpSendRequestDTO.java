package com.dinethdf.ClinicManagementSystem.DTO.Otp;

public class OtpSendRequestDTO {
    private String mobileNo;
    private String email; // Optional
    private Integer clinicId;

    // Getters and Setters
    public String getMobileNo() { return mobileNo; }
    public void setMobileNo(String mobileNo) { this.mobileNo = mobileNo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getClinicId() { return clinicId; }
    public void setClinicId(Integer clinicId) { this.clinicId = clinicId; }
}