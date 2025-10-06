package com.dinethdf.ClinicManagementSystem.DTO.Patient;

import lombok.Data;

@Data
public class PatientResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private String phone;

    public PatientResponseDto(Long id, String fullName, String email, String phone) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }
}

