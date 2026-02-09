package com.dinethdf.ClinicManagementSystem.DTO.Clinic;

import lombok.Data;

@Data
public class ClinicUpdateDTO {

    // Basic Info
    private String name;
    private String address;
    private String city;
    private String district;
    private String mobileNo;
    private String landNo;
    private String email;
    private String website;

    // --- NEW: Settings Fields ---
    // These will be mapped to the JSON 'settings' column in the Service
    private String invoicePrefix;
    private String currency;
    private String timezone;
    private String dateFormat;
}