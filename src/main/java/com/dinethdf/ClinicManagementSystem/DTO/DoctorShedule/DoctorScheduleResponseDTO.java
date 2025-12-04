package com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.DoctorSchedule.ScheduleStatus;
import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.DoctorSchedule.BookingStatus;
import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.DoctorSchedule.PayoutStatus;

@Data
public class DoctorScheduleResponseDTO {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private String specialization;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal doctorFee;
    private BigDecimal clinicFee;
    private Integer maxPatientCount;
    private Integer registeredPatientCount;

    // New fields
    private ScheduleStatus status;
    private BookingStatus bookingStatus;
    private PayoutStatus payoutStatus;
}
