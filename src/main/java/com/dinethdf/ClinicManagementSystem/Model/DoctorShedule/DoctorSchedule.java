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

    @Column(name = "payment", nullable = false, precision = 10, scale = 2)
    private BigDecimal payment;

    @Column(name = "maxPatientCount", nullable = false)
    private Integer maxPatientCount;

    @Column(name = "isActive", nullable = false)
    private Boolean isActive = true;

}
