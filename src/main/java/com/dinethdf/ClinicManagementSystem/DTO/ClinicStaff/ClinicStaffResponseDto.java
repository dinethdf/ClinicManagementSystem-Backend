package com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff;

import com.dinethdf.ClinicManagementSystem.Model.ClinicStaff.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClinicStaffResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNo;
    private String city;
    private String nic;
    private LocalDate dob;
    private String nameWithInitials;
    private String designation;
    private Status status;
    private String profileImg;
    private Integer gender;
}