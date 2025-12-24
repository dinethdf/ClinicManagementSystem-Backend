package com.dinethdf.ClinicManagementSystem.Model.Financial;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "operational_expenses", indexes = {
        @Index(name = "idx_expense_clinic", columnList = "clinic_id")
})
@Data
public class OperationalExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "clinic_id", nullable = false)
    private Integer clinicId; // <--- Multi-Tenant Field

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "expense_category_id", nullable = false)
    private ExpenseCategory expenseCategory;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DoctorSchedulePayment.PayoutStatus status = DoctorSchedulePayment.PayoutStatus.PENDING;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PayoutTransaction> transactions = new ArrayList<>();

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
        this.createdDate = LocalDateTime.now();
    }
}