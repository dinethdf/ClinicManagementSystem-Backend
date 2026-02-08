package com.dinethdf.ClinicManagementSystem.DTO.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PaymentHistoryProjection {
    Integer getPaymentId();
    LocalDateTime getPaymentDate();
    BigDecimal getAmount();
    String getTransactionRef();
    String getReceivedBy();
    String getMethod();
    String getLocation();
    String getStatusCode();
    String getStatusDisplay();
    String getStatusColor();
}
