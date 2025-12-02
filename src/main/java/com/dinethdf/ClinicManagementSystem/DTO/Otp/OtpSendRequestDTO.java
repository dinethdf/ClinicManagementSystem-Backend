package com.dinethdf.ClinicManagementSystem.DTO.Otp;

public class OtpSendRequestDTO {
    private String mobileNo;
    private String email; // Optional

    // Getters and Setters
    public String getMobileNo() { return mobileNo; }
    public void setMobileNo(String mobileNo) { this.mobileNo = mobileNo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}