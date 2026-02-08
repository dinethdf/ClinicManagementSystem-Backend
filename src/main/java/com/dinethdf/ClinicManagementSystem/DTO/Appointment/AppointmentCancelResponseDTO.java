package com.dinethdf.ClinicManagementSystem.DTO.Appointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentCancelResponseDTO {
    private boolean isSuccess;
    private String message;
    private BigDecimal refundAmount;
}