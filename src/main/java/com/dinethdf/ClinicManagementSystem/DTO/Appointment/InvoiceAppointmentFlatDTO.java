// Repo/Invoice/InvoiceFlatDTO.java
package com.dinethdf.ClinicManagementSystem.DTO.Appointment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    LocalDateTime getPaidDate();      // Matches SQL: pm.latestDate AS paidDate
    String getPaymentMethod();        // Matches SQL: pm.latestMethod AS paymentMethod
    BigDecimal getAmountPaid();       // Matches SQL: COALESCE(pm.totalPaid, 0) AS amountPaid
    String getTransactionId();        // Matches SQL: pm.latestTxnId AS transactionId

    // Patient Details
    String getPatientNo();
    String getPatientFirstName();
    String getPatientLastName();
    String getPatientAddress();
    String getPatientCity();
    String getPatientMobile();
    LocalDate getPatientDob();

    // Appointment (Repeats on every row, can be null)
    Long getApptId();
    Long getQueuePosition();
    String getApptNumber(); // Ensure your SQL selects this if you need it (e.g. a.appointment_number as apptNumber)
    String getApptNote();

    LocalDate getScheduleDate();
    LocalTime getScheduleStartTime();
    Long getScheduleId();

    String getDoctorFirstName();
    String getDoctorLastName();
    String getDoctorSpec();

    // Items (Unique per row)
    String getItemDescription();
    String getItemType();
    BigDecimal getItemAmount();
}