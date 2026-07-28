package com.dinethdf.ClinicManagementSystem.DTO.Payment;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LateRefundOverrideDTO {

    private Long invoiceId;

    private BigDecimal refundAmount;

    private String refundMethod; // CASH, CARD, etc.

    private String approvalNote; // Admin MUST give a reason
}
