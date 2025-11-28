// Repo/Invoice/InvoiceFlatDTO.java
package com.dinethdf.ClinicManagementSystem.DTO.Appointment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// This is an "Interface Projection". Spring Data JPA will automatically 
// fill these getter methods from the query column aliases.
public interface InvoiceAppointmentFlatDTO {
    // Invoice & Patient (Repeats on every row)
    Long getInvoiceId();
    LocalDateTime getInvoiceDate();
    String getInvoiceStatus();
    BigDecimal getSubtotal();
    BigDecimal getTotalDiscount();
    BigDecimal getFinalAmount();
    String getPatientNo();
    String getPatientFirstName();
    String getPatientLastName();
    String getPatientAddress();
    String getPatientCity();
    String getPatientMobile();
    LocalDate getPatientDob();

    // Appointment (Repeats on every row, can be null)
    Long getApptId();
    String getApptNumber();
    String getApptNote();
    String getDoctorFirstName();
    String getDoctorLastName();
    String getDoctorSpec();

    // Items (Unique per row)
    String getItemDescription();
    String getItemType();
    BigDecimal getItemAmount();
}