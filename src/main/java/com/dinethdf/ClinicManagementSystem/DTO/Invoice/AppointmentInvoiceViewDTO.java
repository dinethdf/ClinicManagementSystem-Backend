package com.dinethdf.ClinicManagementSystem.DTO.Invoice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class AppointmentInvoiceViewDTO {
    // --- Invoice Header ---
    private Long invoiceId;
    private LocalDateTime dateIssued;
    private LocalDateTime datePaidDate;

    // --- CHANGED: Replaced String with Object ---
    private StatusDTO status;

    // --- 'Bill To' (Patient) Section ---
    private Long patientId;
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

    // --- Payment Details ---
    private String paymentMethod;
    private BigDecimal amountPaid;
    private String transactionId;

    // --- NEW: Nested Status Class ---
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatusDTO {
        private String code;        // e.g., 'PAID' (for logic)
        private String displayName; // e.g., 'Paid' (for display)
        private String colorCode;   // e.g., '#28A745' (for frontend badge)
    }
}