package com.dinethdf.ClinicManagementSystem.DTO.Payment;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponseDTO {
    private Long id;
    private Long invoiceId;

    private BigDecimal amountPaid;
    private BigDecimal remainingBalance; // NEW: To show if they owe more

    private String paymentMethod;   // Display Name (e.g. "Cash")
    private String transactionId;

    // Changed to Object for frontend badges
    private PaymentStatusDTO paymentStatus;

    private LocalDateTime createdDate;
    private String smsStatus;

    @Data
    public static class PaymentStatusDTO {
        private String code;
        private String displayName;
        private String colorCode;
    }
}