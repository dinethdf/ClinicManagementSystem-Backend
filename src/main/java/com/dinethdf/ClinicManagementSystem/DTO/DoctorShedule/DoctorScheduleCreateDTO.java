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
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal doctorFee;
    private BigDecimal clinicFee;

    private Integer maxPatientCount;
    private Integer registeredPatientCount;
}
