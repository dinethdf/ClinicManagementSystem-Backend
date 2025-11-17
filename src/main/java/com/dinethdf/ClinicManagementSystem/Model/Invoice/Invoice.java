package com.dinethdf.ClinicManagementSystem.Model.Invoice;

import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "invoices")
public class Invoice {
    // A minimal entity so our Payment can link to it
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;
}
