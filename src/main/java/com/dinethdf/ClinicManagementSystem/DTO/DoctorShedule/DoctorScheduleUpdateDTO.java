package com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class DoctorScheduleUpdateDTO {
    // Usually, you only allow updating a few fields,
    // not the doctor or date, as that's a "new" schedule.
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal doctorFee;
    private BigDecimal clinicFee;
    private Integer maxPatientCount;
}
