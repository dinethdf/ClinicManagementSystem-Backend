package com.dinethdf.ClinicManagementSystem.Model.Appointment;

import com.dinethdf.ClinicManagementSystem.Model.AuditWrapper; // Import your wrapper
import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.DoctorSchedule;
import com.dinethdf.ClinicManagementSystem.Model.Invoice.Invoice;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@NamedStoredProcedureQuery(
        name = "Appointment.spBookAppointment",
        procedureName = "sp_BookAppointment",
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_createdBy", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_doctor_schedule_id", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_patient_id", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_note", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_queuePosition", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_newAppointmentId", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_newInvoiceId", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_statusMessage", type = String.class)
        }
)
@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_unique_queue", columnList = "doctor_schedule_id, queuePosition", unique = true),
        @Index(name = "idx_patient", columnList = "patient_id")
})
@Data
@EqualsAndHashCode(callSuper = true) // Important for Lombok inheritance
public class Appointment extends AuditWrapper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int queuePosition;

    @Lob
    private String note;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    // --- Relationships ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_schedule_id", nullable = false)
    private DoctorSchedule doctorSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = true)
    private Invoice invoice;

    public enum AppointmentStatus {
        PENDING_PAYMENT, BOOKED, COMPLETED, CANCELLED, NOSHOW
    }
}