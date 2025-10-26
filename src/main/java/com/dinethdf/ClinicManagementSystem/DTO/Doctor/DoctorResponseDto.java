package com.dinethdf.ClinicManagementSystem.DTO.Doctor;

import lombok.Data;
import lombok.NoArgsConstructor; // Make sure to keep this
import java.time.LocalDate;

@Data
@NoArgsConstructor // Keep the no-arg constructor for Jackson
public class DoctorResponseDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNo;
    private String city;
    private String nic;
    private LocalDate dob;
    private String nameWithInitials;
    private String slmcNumber;
    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private String profileImg;

    // --- THIS IS THE NEW CONSTRUCTOR FOR JPA ---
    // The order of arguments MUST match the query in the next step
    public DoctorResponseDto(Long id, String firstName, String lastName, String email,
                             String mobileNo, String city, String nic, LocalDate dob,
                             String nameWithInitials, String slmcNumber,
                             String specialization, String qualification,
                             Integer experienceYears, String profileImg) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobileNo = mobileNo;
        this.city = city;
        this.nic = nic;
        this.dob = dob;
        this.nameWithInitials = nameWithInitials;
        this.slmcNumber = slmcNumber;
        this.specialization = specialization;
        this.qualification = qualification;
        this.experienceYears = experienceYears;
        this.profileImg = profileImg;
    }
}