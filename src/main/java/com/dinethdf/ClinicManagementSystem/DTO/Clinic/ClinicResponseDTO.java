package com.dinethdf.ClinicManagementSystem.DTO.Clinic;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ClinicResponseDTO {
    private Integer id;
    private String name;
    private String subdomain;
    private String address;
    private String city;
    private String mobileNo;
    private String landNo;
    private String email;
    private String logoUrl;
    private String status;
    private String subscriptionPlan;
    private LocalDate expiryDate;
}