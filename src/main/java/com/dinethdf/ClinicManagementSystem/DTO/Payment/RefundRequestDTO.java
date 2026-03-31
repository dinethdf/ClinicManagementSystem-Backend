package com.dinethdf.ClinicManagementSystem.DTO.Payment;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RefundRequestDTO {
    private Long invoiceId;
    private BigDecimal amount;
    private String reason;
    private String authorizedBy;
    private String refundCategory;
    private String refundMethod; // NEW FIELD
}