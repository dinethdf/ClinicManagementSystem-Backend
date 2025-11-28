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
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_discounts_json", type = String.class), // JSON passed as String

                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_new_payment_id", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_status_message", type = String.class)
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false)
    private BigDecimal amountPaid;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String transactionId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false, updatable = false)
    private String createdBy;

    private LocalDateTime updatedDate;
    private String updatedBy;
    private LocalDateTime approvedDate;
    private String approvedBy;

    @Column(nullable = false)
    private Boolean isApproved = false;


    public enum PaymentMethod {
        CASH, CARD, BANK_TRANSFER, ONLINE, INSURANCE
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }
}