package com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DoctorScheduleResponseDTO {
    private Long id;
    private Long doctorId;
    private String doctorName; // Good to include for the frontend
    private LocalDate scheduleDate;
    private LocalTime scheduleTime;
    private BigDecimal payment;
    private Integer maxPatientCount;
    private Boolean isActive;
}