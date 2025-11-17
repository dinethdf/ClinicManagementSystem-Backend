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
    private String specialization; // Good to include for the frontend
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal doctorFee;
    private BigDecimal clinicFee;
    private Integer maxPatientCount;
    private Integer registeredPatientCount;
    private Boolean isActive;
}