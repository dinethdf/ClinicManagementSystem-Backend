package com.dinethdf.ClinicManagementSystem.DTO.Financial;

import com.dinethdf.ClinicManagementSystem.Model.Financial.DoctorSchedulePayment;
import java.math.BigDecimal;
import java.time.LocalDate;

// Note: This is an INTERFACE, not a Class
public interface PendingBillView {

    Integer getPayoutId();
    Integer getScheduleId();
    String getDoctorName();
    LocalDate getScheduleDate();
    BigDecimal getTotalFee();
    BigDecimal getTotalDoctorDiscounts ();
    BigDecimal getFinalPayableAmount();
    DoctorSchedulePayment.PayoutStatus getStatus();
}