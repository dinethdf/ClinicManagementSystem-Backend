package com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule;

import lombok.Data;

@Data
public class DelayNotificationDTO {
    private Integer delayMinutes;
    private String customMessage;
}
