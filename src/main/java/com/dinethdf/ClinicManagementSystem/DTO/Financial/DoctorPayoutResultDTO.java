package com.dinethdf.ClinicManagementSystem.DTO.Financial;

import com.dinethdf.ClinicManagementSystem.Model.Financial.DoctorSchedulePayment;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DoctorPayoutResultDTO {
    private Integer payoutId;
    private Long scheduleId;
    private BigDecimal totalFee;
    private BigDecimal totalDiscounts;
    private BigDecimal finalPayable;
    private DoctorSchedulePayment.PayoutStatus status;
    private String message;
}