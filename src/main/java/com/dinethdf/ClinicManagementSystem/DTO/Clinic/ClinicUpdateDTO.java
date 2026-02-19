package com.dinethdf.ClinicManagementSystem.DTO.Clinic;

import lombok.Data;

@Data
public class ClinicUpdateDTO {
    private String name;

    // --- UPDATED CONTACT FIELDS ---
    private String mobileNo1;
    private String mobileNo2;
    private String landlineNo;

    private String address;
    private String city;
    private String district;
    private String logoUrl;
}