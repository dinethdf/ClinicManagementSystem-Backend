package com.dinethdf.ClinicManagementSystem.DTO.Patient;

import lombok.Data;

@Data
public class PatientPortalBookingRequestDTO {
    private Long scheduleId;
    private String note;
}