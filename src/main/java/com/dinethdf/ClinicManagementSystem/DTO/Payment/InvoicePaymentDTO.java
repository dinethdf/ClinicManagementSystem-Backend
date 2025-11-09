package com.dinethdf.ClinicManagementSystem.DTO.Payment;

import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;

@Data
public class InvoicePaymentDTO {
    private Long invoiceId;

    private String paymentMethod; // "Cash" or "Card"

    // Optional: for card payments
    private String transactionCode;

//    private

    // Can be an empty list or null if no discounts
    private List<DiscountDTO> discounts;
}
