// DTO/Invoice/InvoiceViewDTO.java
package com.dinethdf.ClinicManagementSystem.DTO.Invoice;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class AppointmentInvoiceViewDTO {
    // --- Invoice Header ---
    private Long invoiceId;
    private LocalDate dateIssued;
    private String status; // e.g., 'UNPAID'

    // --- 'Bill To' (Patient) Section ---
    private String patientNo;
    private String patientName;
    private String patientAddress;
    private LocalDate patientDob;
    private String patientContactNo;

    // --- Appointment Details (Optional) ---
    private Long appointmentId;
    private String appointmentNumber;
    private String doctorName;
    private String doctorSpecialization;
    private String appointmentNote; // The note from the booking

    // --- Financials ---
    private List<InvoiceItemViewDTO> items;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal finalAmount;
}