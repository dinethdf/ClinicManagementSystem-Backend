package com.dinethdf.ClinicManagementSystem.DTO.Financial;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleFinancialDTO {
    private Long appointmentId;
    private Integer queueNo;
    private String patientName;
    private String patientMobile;
    private Long invoiceId;
    private String paymentStatus;

    // Financials
    private BigDecimal baseDoctorFee;
    private BigDecimal baseClinicFee;

    private BigDecimal doctorDiscountAmount;
    private BigDecimal netDoctorPayout;

    private BigDecimal clinicDiscountAmount;
    private BigDecimal netClinicProfit;

    private BigDecimal totalPaidByPatient;
}