// DTO/Invoice/AppointmentInvoiceViewDTO.java
package com.dinethdf.ClinicManagementSystem.DTO.Invoice;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class AppointmentInvoiceViewDTO {
    // --- Invoice Header ---
    private Long invoiceId;
    private LocalDateTime dateIssued; // Changed to LocalDateTime (usually invoice creation has time)
    private LocalDateTime datePaidDate; // Changed to LocalDateTime (to match paymentTimestamp)
    private String status; // e.g., 'UNPAID', 'PAID'

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
    private String appointmentNote;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private Long queuePosition;
    private Long scheduleId;
    // --- Financials ---
    private List<InvoiceItemViewDTO> items;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal finalAmount;

    // --- Payment Details (NEW) ---
    private String paymentMethod;   // e.g. "CASH", "CARD"
    private BigDecimal amountPaid;  // Total paid amount
    private String transactionId;   // e.g. "TXN-12345"
}