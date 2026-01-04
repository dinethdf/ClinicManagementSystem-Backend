package com.dinethdf.ClinicManagementSystem.DTO.Lab;

import com.dinethdf.ClinicManagementSystem.Model.Lab.PatientLabRequest.LabStatus;
import lombok.Data;

@Data
public class LabRequestUpdateDTO {

    // Used to move status from PENDING -> SAMPLE_COLLECTED -> COMPLETED
    private LabStatus status;

    // Used when results are ready
    private String technicianNotes;
    private String reportUrl;
}