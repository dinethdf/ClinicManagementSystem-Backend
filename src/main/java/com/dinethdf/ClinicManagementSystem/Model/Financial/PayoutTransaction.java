package com.dinethdf.ClinicManagementSystem.Model.Financial;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payout_transactions")
@Data
public class PayoutTransaction {

    // --- INNER ENUM ---
    public enum PaymentMethod {
        CASH,
        CARD,
        BANK_TRANSFER,
        ONLINE,
        CHEQUE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // --- TENANT ISOLATION (New) ---
    @Column(name = "clinic_id", nullable = false)
    private Integer clinicId;

    // --- FOREIGN KEYS ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_payment_id")
    private DoctorSchedulePayment doctorPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_payout_id")
    private LabVendorPayout labPayout;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    private OperationalExpense expense;

    // --- PAYMENT DETAILS ---
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    private String transactionReference;

    @Column(nullable = false)
    private LocalDateTime paymentDate = LocalDateTime.now();

    // --- SYSTEM COLUMNS ---
    @Column(nullable = false)
    private LocalDateTime createdDate;
    @Column(nullable = false, length = 50)
    private String createdBy;
    private LocalDateTime updatedDate;
    private String updatedBy;
    private LocalDateTime approvedDate;
    private String approvedBy;

    @PrePersist
    protected void onCreate() {
        if (this.createdDate == null) {
            this.createdDate = LocalDateTime.now();
        }
    }
}