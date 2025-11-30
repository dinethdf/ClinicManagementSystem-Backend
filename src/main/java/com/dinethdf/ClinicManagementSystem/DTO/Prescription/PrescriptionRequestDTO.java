package com.dinethdf.ClinicManagementSystem.DTO.Prescription;

import lombok.Data;
import java.util.List;

@Data
public class PrescriptionRequestDTO {
    private Long appointmentId;
    private String note;
    private List<PrescriptionItemDTO> items;
}