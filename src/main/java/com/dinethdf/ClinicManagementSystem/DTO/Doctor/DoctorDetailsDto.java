package com.dinethdf.ClinicManagementSystem.DTO.Doctor;

import com.dinethdf.ClinicManagementSystem.Model.Doctor.Specialization;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class DoctorDetailsDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String nameWithInitials;
    private String email;
    private String mobileNo;
    private String nic;
    private LocalDate dob;
    private Integer gender;
    private String address;
    private String city;
    private String district;
    private String slmcNumber;
    private Specialization specialization;
    private String qualification;
    private Integer experienceYears;
    private String profileImg;
    private String availabilityStatus;

    // Optional system fields if needed
    private Boolean isApproved;
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime updatedDate;
    private String updatedBy;
    private LocalDateTime approvedDate;
    private String approvedBy;
}

