package com.dinethdf.ClinicManagementSystem.DTO.Financial;

import lombok.Data;
import java.math.BigDecimal;

// OUTPUT: What the Admin sees after clicking "Pay"
@Data
public class BulkPaymentResponseDTO {
    private int successCount;
    private BigDecimal totalProcessedAmount;
    private String transactionReference;
    private String message;
}