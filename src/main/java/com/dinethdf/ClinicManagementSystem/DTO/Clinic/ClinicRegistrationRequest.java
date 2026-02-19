package com.dinethdf.ClinicManagementSystem.DTO.Clinic;

import lombok.Data;

@Data
public class ClinicRegistrationRequest {
    private String clinicName;
    private String subdomain;

    // Updated Contact Fields
    private String mobileNo1;
    private String mobileNo2;
    private String landlineNo;

    private String address;
    private String city;

    private String adminFirstName;
    private String adminLastName;
    private String email;     // This will be the Username
    private String password;
}