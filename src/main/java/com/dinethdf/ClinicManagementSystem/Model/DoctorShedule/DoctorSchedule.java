package com.dinethdf.ClinicManagementSystem.Model.DoctorShedule;

import com.dinethdf.ClinicManagementSystem.Model.AuditWrapper;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Doctor;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "doctorSchedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class DoctorSchedule extends AuditWrapper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Doctor reference (foreign key)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "scheduleDate", nullable = false)
    private LocalDate scheduleDate;

    @Column(name = "startTime", nullable = false)
    private LocalTime startTime;

    @Column(name = "endTime", nullable = false)
    private LocalTime endTime;

    @Column(name = "doctorFee", nullable = false, precision = 10, scale = 2)
    private BigDecimal doctorFee;

    @Column(name = "clinicFee", nullable = false, precision = 10, scale = 2)
    private BigDecimal clinicFee;

    @Column(name = "maxPatientCount", nullable = false)
    private Integer maxPatientCount;

    @Column(name = "registeredPatientCount", nullable = false)
    private Integer registeredPatientCount = 0;

//    @Column(name = "isActive", nullable = false)
//    private Boolean isActive = true;


    // --- New Columns ---

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScheduleStatus status = ScheduleStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "bookingStatus", nullable = false)
    private BookingStatus bookingStatus = BookingStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "payoutStatus", nullable = false)
    private PayoutStatus payoutStatus = PayoutStatus.NOT_PAID;

    public enum ScheduleStatus {
        ACTIVE,
        COMPLETED,
        CANCELLED
    }

    public enum BookingStatus {
        OPEN,
        CLOSED
    }

    public enum PayoutStatus {
        NOT_PAID,
        PENDING,
        PAID
    }

}



