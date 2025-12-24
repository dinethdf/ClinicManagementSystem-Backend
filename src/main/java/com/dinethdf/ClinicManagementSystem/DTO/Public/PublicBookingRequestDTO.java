package com.dinethdf.ClinicManagementSystem.DTO.Public;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PublicBookingRequestDTO {

    private Long scheduleId;
    private String note;

    private Long existingPatientId;
    private int gender;

    private String firstName;
    private String lastName;
    private String nic;
    private LocalDate dob;
    private String mobileNo;
    private String email;
    private Integer clinicId;
}