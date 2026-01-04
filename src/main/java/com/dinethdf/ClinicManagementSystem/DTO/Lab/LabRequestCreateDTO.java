package com.dinethdf.ClinicManagementSystem.DTO.Lab;

import lombok.Data;

@Data
public class LabRequestCreateDTO {

    private Integer patientId;
    private Integer labTestTypeId;

    private Integer appointmentId;
}