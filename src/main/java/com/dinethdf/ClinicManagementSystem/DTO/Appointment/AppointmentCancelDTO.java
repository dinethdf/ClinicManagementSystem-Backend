package com.dinethdf.ClinicManagementSystem.DTO.Appointment;

import lombok.Data;

@Data
public class AppointmentCancelDTO {
    private String reason;
    private String refundMethod; // Optional: "CASH", "CARD", "ORIGINAL_METHOD"
}