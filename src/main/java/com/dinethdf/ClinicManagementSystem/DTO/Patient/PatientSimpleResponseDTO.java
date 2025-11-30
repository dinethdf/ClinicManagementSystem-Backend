package com.dinethdf.ClinicManagementSystem.DTO.Patient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientSimpleResponseDTO {
    private Long id;
    private String patientNo;
    private String firstName;
    private String lastName;
}