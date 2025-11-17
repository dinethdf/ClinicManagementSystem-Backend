package com.dinethdf.ClinicManagementSystem.DTO.Appointment;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentResponseDTO {

    private Long id;
//    private Long appointmentId;
    private int queuePosition;
    private String status;
    private String note;
    private LocalDateTime createdDate;

    // We can return the IDs for reference
    private Long patientId;
    private Long doctorScheduleId;

    // You could also add patientName or doctorName here
}