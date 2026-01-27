package com.dinethdf.ClinicManagementSystem.DTO.Clinic;

import lombok.Data;

@Data
public class ClinicUpdateDTO {
    private String name;
    private String address;
    private String city;
    private String district;
    private String mobileNo;
    private String landNo;
    private String email;
    private String website;
}