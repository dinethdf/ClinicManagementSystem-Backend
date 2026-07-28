package com.dinethdf.ClinicManagementSystem.DTO.Payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LateRefundOverrideDTO {

    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than zero")
    private BigDecimal refundAmount;

    @NotBlank(message = "Refund method is required")
    private String refundMethod; // CASH, CARD, etc.

    @NotBlank(message = "Approval note is mandatory for late refunds")
    private String approvalNote; // Admin MUST give a reason
}
