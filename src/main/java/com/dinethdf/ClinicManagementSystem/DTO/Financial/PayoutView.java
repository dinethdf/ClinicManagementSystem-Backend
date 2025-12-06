package com.dinethdf.ClinicManagementSystem.DTO.Financial;

import com.dinethdf.ClinicManagementSystem.Model.Financial.DoctorSchedulePayment;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface PayoutView {
    Integer getPayoutId();
    Integer getScheduleId();
    String getDoctorName();
    LocalDate getScheduleDate();
    BigDecimal getTotalFee();
    BigDecimal getFinalPayableAmount();
    DoctorSchedulePayment.PayoutStatus getStatus();
    String getCreatedBy(); // Good for auditing
}