package com.dinethdf.ClinicManagementSystem.DTO.Payment;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponseDTO {
    private Long id;
    private Long invoiceId;
    private BigDecimal amountPaid;
    private String paymentMethod;
    private String transactionId;
    private String paymentStatus;
    private LocalDateTime createdDate;
    private String smsStatus;
}
