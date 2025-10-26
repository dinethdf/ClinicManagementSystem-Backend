package com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DoctorScheduleCreateDTO {
    // We only need the ID, not the whole Doctor object
    private Long doctorId;
    private LocalDate scheduleDate;
    private LocalTime scheduleTime;
    private BigDecimal payment;
    private Integer maxPatientCount;
}
