package com.dinethdf.ClinicManagementSystem.DTO.Doctor;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DoctorUpdateDto {
    private String firstName;
    private String lastName;
    private String nameWithInitials;
    private String email;
    private String mobileNo;
    private String nic;
    private LocalDate dob;

    // New fields from your DB
    private Integer gender;
    private String address;
    private String city;
    private String district;

    private String slmcNumber;
    private Integer specializationId;
    private String qualification;
    private Integer experienceYears;
}