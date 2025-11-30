package com.dinethdf.ClinicManagementSystem.DTO.Prescription;

import lombok.Data;

@Data
public class PrescriptionItemDTO {
    private String medicineName;
    private String dosage;
    private String quantity;
    private String duration;
    private String instructions;
}