package com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AffectedPatientDTO {
    private Long appointmentId;
    private String patientName;
    private String patientNo;
    private String patientMobile;
    private int queuePosition;
}
