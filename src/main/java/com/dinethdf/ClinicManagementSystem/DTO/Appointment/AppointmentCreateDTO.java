package com.dinethdf.ClinicManagementSystem.DTO.Appointment;

import lombok.Data;

@Data
public class AppointmentCreateDTO {

    private Long doctorScheduleId;
    private Long patientId;
    private String note;
}