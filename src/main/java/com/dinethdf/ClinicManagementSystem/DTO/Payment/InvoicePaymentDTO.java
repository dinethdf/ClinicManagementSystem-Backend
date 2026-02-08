package com.dinethdf.ClinicManagementSystem.DTO.Payment;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class InvoicePaymentDTO {

    // Identifiers
    private Long invoiceId;

    // Financials
    private BigDecimal amountPaid;        // NEW: Mandatory for Partial Payments

    // Payment Details
    private String paymentMethod;         // "CASH", "CARD", "ONLINE" (Must match Lookup Code)
    private String paymentLocation;       // NEW: "CLINIC_COUNTER", "ONLINE" (Must match Lookup Code)

    // Transaction Details (Optional)
    private String transactionCode;       // e.g., "TXN-12345"
    private String paymentGateway;        // NEW: Optional (e.g., "Stripe") if method is Online

    // Staff Info
    private Integer receivedByStaffId;    // NEW: ID of the logged-in user taking payment

    // Discounts (Optional)
    private List<DiscountDTO> discounts;  // Can be empty/null

}