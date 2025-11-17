// DTO/Invoice/InvoiceItemViewDTO.java
package com.dinethdf.ClinicManagementSystem.DTO.Invoice;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InvoiceItemViewDTO {
    private String description;
    private String typeName; // e.g., 'APPOINTMENT', 'LABTEST'
    private BigDecimal unitPrice;
    private int quantity; // Typically 1 for services
    private BigDecimal totalAmount;
}