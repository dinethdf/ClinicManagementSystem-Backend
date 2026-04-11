package com.dinethdf.ClinicManagementSystem.DTO.Financial;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleFinancialDTO {

    // 1. Appointment & Patient Details
    private Long appointmentId;
    private Integer queueNo;
    private String patientName;
    private String patientMobile;
    private Long invoiceId;

    // 2. Status Information (For UI Badges)
    private String paymentStatus;
    private String paymentStatusCode;
    private String paymentStatusColor;

    private String appointmentStatus;
    private String appointmentStatusCode;
    private String appointmentStatusColor;

    // 3. Base Fees
    private BigDecimal baseDoctorFee;
    private BigDecimal baseClinicFee;

    // 4. Doctor Side Calculations
    private BigDecimal doctorDiscountAmount;
    private BigDecimal netDoctorPayout;

    // 5. Clinic Side Calculations
    private BigDecimal clinicDiscountAmount;
    private BigDecimal netClinicProfit;

    // 6. Total Bill & Payment Tracking
    private BigDecimal subtotalAmount;
    private BigDecimal totalDiscount;
    private BigDecimal totalPaidByPatient;
    private BigDecimal amountPaid;
    private BigDecimal balanceRemaining;
    private LocalDate paymentDueDate;
    private BigDecimal paymentPercentage;

    // 7. Last Payment Details
    private String lastPaymentMethod;
    private String lastPaymentMethodCode;
    private String lastPaymentLocation;
    private LocalDateTime lastPaymentDate;
    private Integer totalPayments;
}