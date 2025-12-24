package com.dinethdf.ClinicManagementSystem.DTO.Otp;

public class OtpVerifyRequestDTO {
    private String mobileNo;
    private String otp;
    private Integer clinicId;

    // Getters and Setters
    public String getMobileNo() { return mobileNo; }
    public void setMobileNo(String mobileNo) { this.mobileNo = mobileNo; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public Integer getClinicId() { return clinicId; }
    public void setClinicId(Integer clinicId) { this.clinicId = clinicId; }
}