package com.dinethdf.ClinicManagementSystem.Model.DoctorShedule;

import com.dinethdf.ClinicManagementSystem.Model.AuditWrapper;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Doctor;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "doctorSchedule", indexes = {
        @Index(name = "idx_schedule_lookup", columnList = "clinic_id, doctor_id, scheduleDate"),
        @Index(name = "idx_schedule_date", columnList = "clinic_id, scheduleDate")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class DoctorSchedule extends AuditWrapper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clinic_id", nullable = false)
    private Integer clinicId;

    // Doctor reference
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

    // --- NEW: Queue Management ---
    @Column(name = "lastQueueNumber", nullable = false)
    private Integer lastQueueNumber = 0;

    // --- NEW: Status Relationships (Replacing Enum) ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statusId", nullable = true)
    private ScheduleStatusLookup status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payoutStatusId", nullable = true)
    private PayoutStatusLookup payoutStatus;

    // --- NEW: Flags (Replacing Enums/Missing fields) ---

    @Column(name = "isBookingOpen", nullable = false)
    private Boolean isBookingOpen = true;

    @Column(name = "isActive")
    private Boolean isActive = true;

    @Column(name = "isApproved", nullable = false)
    private Boolean isApproved = false;

    // Note: removed inner Enums as they are now Lookup Tables
}