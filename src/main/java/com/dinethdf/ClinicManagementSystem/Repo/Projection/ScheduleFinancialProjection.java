package com.dinethdf.ClinicManagementSystem.Repo.Projection;
import java.math.BigDecimal;

public interface ScheduleFinancialProjection {
    Long getAppointmentID();
    Integer getQueueNo();
    String getPatientName();
    String getPatientMobile();
    Long getInvoiceID();
    String getPaymentStatus();

    BigDecimal getBaseDoctorFee();
    BigDecimal getBaseClinicFee();

    BigDecimal getDoctorDiscountAmount();
    BigDecimal getNetDoctorPayout();

    BigDecimal getClinicDiscountAmount();
    BigDecimal getNetClinicProfit();

    BigDecimal getTotalPaidByPatient();
}