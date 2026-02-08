package com.dinethdf.ClinicManagementSystem.DTO.Payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePaymentDetailDTO {

    private Integer paymentId;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
    private String transactionRef;
    private String receivedBy;
    private String method;      // e.g. "Cash", "Card"
    private String location;    // e.g. "Reception"

    // Rich Status Object for UI Badges
    private PaymentStatusDTO status;

    @Data
    @AllArgsConstructor
    public static class PaymentStatusDTO {
        private String code;        // e.g. "COMPLETED", "REFUNDED"
        private String displayName; // e.g. "Completed"
        private String colorCode;   // e.g. "#28a745"
    }
}