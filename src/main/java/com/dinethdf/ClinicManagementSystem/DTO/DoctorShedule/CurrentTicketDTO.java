package com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentTicketDTO {
    private Long appointmentId;
    private Integer queuePosition;
    private String patientName;
    private String status;
}
