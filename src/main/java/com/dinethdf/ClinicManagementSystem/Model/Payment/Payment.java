package com.dinethdf.ClinicManagementSystem.Model.Payment;

import com.dinethdf.ClinicManagementSystem.Model.Invoice.Invoice;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payments")
// This defines the Stored Procedure for JPA
@NamedStoredProcedureQuery(
        name = "Payment.sp_AppointmentPaymentProcess",
        procedureName = "sp_AppointmentPaymentProcess",
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_invoice_id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_payment_method", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_transaction_code", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_created_by", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_discounts_json", type = String.class),

                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_new_payment_id", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_status_message", type = String.class)
        }
)
public class Payment {
    // This entity just defines the table structure
    // and the SP. We don't use AuditWrapper because
    // the SP handles the audit fields manually.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false)
    private BigDecimal amountPaid;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private String paymentStatus;

    private String transactionId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false, updatable = false)
    private String createdBy;

    // ... other audit fields
}