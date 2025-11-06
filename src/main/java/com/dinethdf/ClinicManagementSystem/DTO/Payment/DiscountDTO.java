// DTO/Payment/DiscountDTO.java
package com.dinethdf.ClinicManagementSystem.DTO.Payment;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DiscountDTO {
    // Must match a typeName in your item_types table
    // e.g., "DOCTOR_DISCOUNT" or "CLINIC_DISCOUNT"
    private String discountType;
    private BigDecimal amount;
    private String description;
}