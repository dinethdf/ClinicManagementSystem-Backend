package com.dinethdf.ClinicManagementSystem.Model.Invoice;

import com.dinethdf.ClinicManagementSystem.Model.AuditWrapper;
import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "invoices", indexes = {
        @Index(name = "idx_invoice_clinic", columnList = "clinic_id")
})
public class Invoice extends AuditWrapper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clinic_id", nullable = false)
    private Integer clinicId;

    // --- CHANGED: Replaced Enum with Lookup Entity Relationship ---
    @ManyToOne(fetch = FetchType.EAGER) // Eager is fine for small lookup tables
    @JoinColumn(name = "statusId", referencedColumnName = "id", nullable = false)
    private InvoiceStatusLookup status;

    @Column(nullable = false)
    private BigDecimal subtotalAmount;

    @Column(nullable = false)
    private BigDecimal totalDiscount;

    @Column(nullable = false)
    private BigDecimal finalAmount;

    private LocalDate dueDate;

    @Column(nullable = false)
    private Boolean isApproved = false;

    @Column(precision = 10, scale = 2)
    private BigDecimal balanceRemaining;

    private Long receivedByStaffId;

    private String cashierWorkstationId;

    // ... Other mappings remain the same ...
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @OneToOne(mappedBy = "invoice", fetch = FetchType.LAZY)
    private Appointment appointment;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InvoiceItem> invoiceItems;

    // Remove the inner Enum since we use the DB table now
}