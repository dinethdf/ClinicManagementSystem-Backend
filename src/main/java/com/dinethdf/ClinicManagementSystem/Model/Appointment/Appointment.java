package com.dinethdf.ClinicManagementSystem.Model.Appointment;

import com.dinethdf.ClinicManagementSystem.Model.AuditWrapper;
import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.DoctorSchedule;
import com.dinethdf.ClinicManagementSystem.Model.Invoice.Invoice;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@NamedStoredProcedureQuery(
        name = "Appointment.spBookAppointment",
        procedureName = "sp_BookAppointment",
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_clinic_id", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_createdBy", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_doctor_schedule_id", type = Integer.class), // Changed Long to Integer to match SP
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_patient_id", type = Integer.class),         // Changed Long to Integer to match SP
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_note", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_queuePosition", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_newAppointmentId", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_newInvoiceId", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_statusMessage", type = String.class)
        }
)

@NamedStoredProcedureQuery(
        name = "Appointment.cancel",
        procedureName = "sp_CancelAppointment",
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_clinic_id", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_appointment_id", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_cancel_reason", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_cancelled_by", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_refund_method", type = String.class), // <--- NEW
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_refund_amount", type = BigDecimal.class), // <--- NEW
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "o_status_message", type = String.class)
        }
)

@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_unique_queue", columnList = "clinic_id, doctor_schedule_id, queuePosition", unique = true),
        @Index(name = "idx_patient_lookup", columnList = "clinic_id, patient_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class Appointment extends AuditWrapper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clinic_id", nullable = false)
    private Integer clinicId;

    @Column(nullable = false)
    private int queuePosition;

    @Lob
    private String note;

    @Column(nullable = false)
    private Boolean isApproved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statusId", nullable = true)
    private AppointmentStatusLookup status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_schedule_id", nullable = false)
    private DoctorSchedule doctorSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = true)
    private Invoice invoice;
}