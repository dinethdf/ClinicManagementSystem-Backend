package com.dinethdf.ClinicManagementSystem.Model.Financial;

import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.DoctorSchedule;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctor_schedule_payments", indexes = {
        @Index(name = "idx_payout_clinic", columnList = "clinic_id")
})
@Data
public class DoctorSchedulePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "clinic_id", nullable = false)
    private Integer clinicId; // <--- Multi-Tenant Field

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_schedule_id", nullable = false, unique = true)
    private DoctorSchedule doctorSchedule;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDoctorFee = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDoctorDiscounts = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPayableAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayoutStatus status = PayoutStatus.PENDING;

    @OneToMany(mappedBy = "doctorPayment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
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

    public enum PayoutStatus {
        PENDING,
        PARTIAL,
        PAID
    }

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}