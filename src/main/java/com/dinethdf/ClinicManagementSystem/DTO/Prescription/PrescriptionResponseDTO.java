package com.dinethdf.ClinicManagementSystem.DTO.Prescription;

import com.dinethdf.ClinicManagementSystem.Model.Prescription.PrescriptionStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PrescriptionResponseDTO {
    private Long id;
    private Long appointmentId;
    private String doctorName;
    private String patientName;
    private PrescriptionStatus status;
    private String note;
    private LocalDateTime createdDate;
    private String createdBy;

    private List<PrescriptionItemDTO> items;
}