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
@Table(name = "lab_vendor_payouts")
@Data
public class LabVendorPayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String vendorName;

    private String description;

    private LocalDate fromDate;
    private LocalDate toDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalBillAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPayableAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DoctorSchedulePayment.PayoutStatus status = DoctorSchedulePayment.PayoutStatus.PENDING;

    @OneToMany(mappedBy = "labPayout", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
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