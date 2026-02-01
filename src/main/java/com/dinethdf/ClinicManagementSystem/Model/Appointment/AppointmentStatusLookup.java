package com.dinethdf.ClinicManagementSystem.Model.Appointment;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "appointment_status_lookup")
@Data
public class AppointmentStatusLookup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // e.g., "PENDING_PAYMENT"

    @Column(nullable = false, length = 100)
    private String displayName; // e.g., "Pending Payment"

    @Column(name = "colorCode", length = 20)
    private String colorCode; // e.g., "#FFA500"
}