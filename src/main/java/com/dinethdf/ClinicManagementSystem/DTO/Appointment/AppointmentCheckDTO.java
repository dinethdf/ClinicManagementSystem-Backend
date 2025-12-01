package com.dinethdf.ClinicManagementSystem.DTO.Appointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentCheckDTO {
    private Long id;
    private Long appointmentId;
    private String fullName;
    private String patientNo;
    private String nic;
    private Integer queueNo;
    private String status;
    private LocalDateTime createdDate;
}