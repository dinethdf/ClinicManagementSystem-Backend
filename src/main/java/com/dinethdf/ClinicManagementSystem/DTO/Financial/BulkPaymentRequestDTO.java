package com.dinethdf.ClinicManagementSystem.DTO.Financial;

import com.dinethdf.ClinicManagementSystem.Model.Financial.PayoutTransaction;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BulkPaymentRequestDTO {
    private List<Integer> paymentIds;
    // Referencing the Inner Enum
    private PayoutTransaction.PaymentMethod paymentMethod;
    private String transactionReference;
    private BigDecimal totalAmountToPay;
}