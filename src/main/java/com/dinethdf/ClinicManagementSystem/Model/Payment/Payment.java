package com.dinethdf.ClinicManagementSystem.Model.Payment;

import com.dinethdf.ClinicManagementSystem.Model.Invoice.Invoice;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_clinic", columnList = "clinic_id"),
        @Index(name = "idx_payment_status", columnList = "paymentStatusId")
})
@NamedStoredProcedureQuery(
        name = "Payment.sp_AppointmentPaymentProcess",
        procedureName = "sp_AppointmentPaymentProcess",
        parameters = {
                // IN Parameters (Must match SP order exactly)
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_clinic_id", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_invoice_id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_amount_paid", type = BigDecimal.class), // NEW
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_payment_method", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_transaction_code", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_created_by", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_received_by", type = String.class),     // NEW
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_received_by_staff_id", type = Integer.class), // NEW
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_payment_location", type = String.class), // NEW
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_payment_gateway", type = String.class),  // NEW
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_discounts_json", type = String.class),

                // OUT Parameters
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_new_payment_id", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_remaining_balance", type = BigDecimal.class), // NEW
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_status_message", type = String.class)
        }
)

// Add this inside the @NamedStoredProcedureQuery parameters list in Payment.java
// (Keep the existing sp_AppointmentPaymentProcess definition too!)

@NamedStoredProcedureQuery(
        name = "Payment.sp_ProcessRefund",
        procedureName = "sp_ProcessRefund",
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_clinic_id", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_invoice_id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_refund_amount", type = BigDecimal.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_refund_category", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_refund_method", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_reason", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_authorized_by", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_created_by", type = String.class),

                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_refund_id", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_status_message", type = String.class)
        }
)
// ... public class Payment ...

public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clinic_id", nullable = false)
    private Integer clinicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false)
    private BigDecimal amountPaid;

    // --- REPLACED: Enum with Lookup Entity ---
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paymentMethodId", referencedColumnName = "id")
    private PaymentMethodLookup paymentMethod;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paymentStatusId", referencedColumnName = "id")
    private PaymentStatusLookup paymentStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paymentLocationId", referencedColumnName = "id")
    private PaymentLocationLookup paymentLocation;

    // --- NEW FIELDS ---
    @Column(name = "receivedBy")
    private String receivedBy; // Name of cashier

    @Column(name = "cashierWorkstationId")
    private Integer cashierWorkstationId; // Stores the Staff ID

    @Column(name = "paymentGateway")
    private String paymentGateway; // e.g. "STRIPE"

    @Column(name = "transactionId")
    private String transactionId;

    // --- AUDIT FIELDS ---
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false, updatable = false)
    private String createdBy;

    // --- PrePersist ---
    @PrePersist
    protected void onCreate() {
        if (createdDate == null) createdDate = LocalDateTime.now();
    }
}