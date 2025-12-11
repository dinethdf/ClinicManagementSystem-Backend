package com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff;


import lombok.Data;
import java.time.LocalDate;

@Data
public class ClinicStaffUpdateDto {
    private String firstName;
    private String lastName;
    private String nameWithInitials;
    private Integer gender;
    private LocalDate dob;
    private String nic;
    private String email;
    private String mobileNo;
    private String address;
    private String city;
    private String district;
    private String designation;
}
