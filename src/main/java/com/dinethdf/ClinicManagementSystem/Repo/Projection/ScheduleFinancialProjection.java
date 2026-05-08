package com.dinethdf.ClinicManagementSystem.Repo.Projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ScheduleFinancialProjection {

    // 1. Appointment & Patient Details
    Long getAppointmentID();
    Integer getQueueNo();
    String getPatientName();
    String getPatientMobile();
    Long getInvoiceID();
    String getPaymentStatus();
    String getPaymentStatusCode();
    String getPaymentStatusColor();

    String getAppointmentStatus();
    String getAppointmentStatusCode();
    String getAppointmentStatusColor();

    // 3. Base Fees
    BigDecimal getBaseDoctorFee();
    BigDecimal getBaseClinicFee();

    // 4. Doctor Side Calculations
    BigDecimal getDoctorDiscountAmount();
    BigDecimal getNetDoctorPayout();

    // 5. Clinic Side Calculations
    BigDecimal getClinicDiscountAmount();
    BigDecimal getNetClinicProfit();

    // 6. Total Bill & Payment Tracking (The new V2.0 Billing Engine)
    BigDecimal getSubtotalAmount();
    BigDecimal getTotalDiscount();
    BigDecimal getTotalPaidByPatient(); // Maps to `inv.finalAmount`
    BigDecimal getAmountPaid();         // What they actually handed over
    BigDecimal getBalanceRemaining();   // What they still owe
    LocalDate getPaymentDueDate();
    BigDecimal getPaymentPercentage();  // Good for UI progress bars

    // 7. Last Payment Details (Includes Refunds)
    String getLastPaymentMethod();
    String getLastPaymentMethodCode();
    String getLastPaymentLocation();
    LocalDateTime getLastPaymentDate();
    Integer getTotalPayments();
}