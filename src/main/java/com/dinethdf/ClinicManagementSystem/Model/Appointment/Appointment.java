package com.dinethdf.ClinicManagementSystem.Model.Appointment;

import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.DoctorSchedule;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

// This annotation defines the Stored Procedure for JPA
@NamedStoredProcedureQuery(
        name = "Appointment.spBookAppointment",
        procedureName = "sp_BookAppointment",
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_createdBy", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_doctor_schedule_id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_patient_id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_note", type = String.class),

                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_queuePosition", type = Integer.class),
//                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_appointmentNumber", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_newAppointmentId", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_newInvoiceId", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_statusMessage", type = String.class)
        }
)
@Entity
@Table(name = "appointments")
@Data
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Column(nullable = false, unique = true)
//    private String appointmentNumber;

    @Column(nullable = false)
    private int queuePosition;

    @Lob
    private String note;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    // --- bookingTimestamp field removed ---

    // --- Relationships ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_schedule_id", nullable = false)
    private DoctorSchedule doctorSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // --- Manual Audit Columns ---
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false, updatable = false)
    private String createdBy;

    private LocalDateTime updatedDate;
    private String updatedBy;
    private LocalDateTime approvedDate;
    private String approvedBy;

    public enum AppointmentStatus {
        PENDING_PAYMENT,
        Booked, // Your preferred status
        COMPLETED,
        CANCELLED,
        NOSHOW
    }

}
