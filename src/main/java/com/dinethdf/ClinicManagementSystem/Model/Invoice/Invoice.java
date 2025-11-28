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
@Table(name = "invoices")
public class Invoice extends AuditWrapper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @Column(nullable = false)
    private BigDecimal subtotalAmount;

    @Column(nullable = false)
    private BigDecimal totalDiscount;

    @Column(nullable = false)
    private BigDecimal finalAmount;

    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @OneToOne(mappedBy = "invoice", fetch = FetchType.LAZY)
    private Appointment appointment;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InvoiceItem> invoiceItems;

    public enum InvoiceStatus {
        UNPAID, PAID, PARTIAL, CANCELLED
    }

    // Override PrePersist to set defaults for financial fields specifically
    @Override
    public void prePersist() {
        super.prePersist();
        if (subtotalAmount == null) subtotalAmount = BigDecimal.ZERO;
        if (totalDiscount == null) totalDiscount = BigDecimal.ZERO;
        if (finalAmount == null) finalAmount = BigDecimal.ZERO;
        if (status == null) status = InvoiceStatus.UNPAID;
    }
}