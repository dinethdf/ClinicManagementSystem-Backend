package com.dinethdf.ClinicManagementSystem.DTO.Appointment;

import java.time.LocalDateTime;

// For Native Queries, use an Interface (Spring maps SQL aliases to these getters)
public interface AppointmentCheckDTO {

    Long getPatientId();        // Matches SQL: p.id AS patientId
    Long getAppointmentId();    // Matches SQL: a.id AS appointmentId
    String getPatientName();    // Matches SQL: CONCAT(...) AS patientName
    String getPatientNo();      // Matches SQL: p.patientNo AS patientNo
    String getNic();            // Matches SQL: p.nic AS nic
    Integer getQueuePosition(); // Matches SQL: a.queuePosition AS queuePosition
    LocalDateTime getCreatedDate(); // Matches SQL: a.createdDate

    // --- Status Fields ---
    String getStatusCode();        // Matches SQL: asl.code AS statusCode
    String getStatusDisplayName(); // Matches SQL: asl.displayName AS statusDisplayName
    String getStatusColor();       // Matches SQL: asl.colorCode AS statusColor
}