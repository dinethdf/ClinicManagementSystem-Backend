package com.dinethdf.ClinicManagementSystem.Model.Lab;

import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import com.dinethdf.ClinicManagementSystem.Model.AuditWrapper;
import com.dinethdf.ClinicManagementSystem.Model.Invoice.Invoice; // Ensure you have this Model
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient; // Ensure you have this Model
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_lab_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PatientLabRequest extends AuditWrapper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "clinic_id", nullable = false)
    private Integer clinicId;

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_test_type_id", nullable = false)
    private LabTestType labTestType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id") // Nullable (for walk-ins)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id") // Nullable (linked later)
    private Invoice invoice;

    // --- Status Enums ---

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('PENDING', 'SAMPLE_COLLECTED', 'COMPLETED', 'CANCELLED')")
    private LabStatus status = LabStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", columnDefinition = "ENUM('PENDING', 'PAID', 'REFUND')")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // --- Results & Financials ---

    @Column(name = "technician_notes", columnDefinition = "TEXT")
    private String technicianNotes;

    @Column(name = "report_url", length = 500)
    private String reportUrl;

    @Column(name = "billed_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal billedAmount;

    @Column(name = "completedDate")
    private LocalDateTime completedDate;

    // --- Inner Enums ---

    public enum LabStatus {
        PENDING,
        SAMPLE_COLLECTED,
        COMPLETED,
        CANCELLED
    }

    public enum PaymentStatus {
        PENDING,
        PAID,
        REFUND
    }
}