package com.dinethdf.ClinicManagementSystem.Repo.Appointment;

import com.dinethdf.ClinicManagementSystem.DTO.Appointment.PatientAppointmentDTO;
import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import com.dinethdf.ClinicManagementSystem.DTO.Appointment.InvoiceAppointmentFlatDTO;
import org.springframework.data.jpa.repository.JpaRepository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface AppointmentRepo extends JpaRepository<Appointment, Long> {

    @Procedure(name = "Appointment.spBookAppointment")
    Map<String, Object> sp_BookAppointment(
            @Param("p_createdBy") String createdBy,
            @Param("p_doctor_schedule_id") Long doctorScheduleId,
            @Param("p_patient_id") Long patientId,
            @Param("p_note") String note
    );

    // Add this optimized query to just get the status Enum
    @Query("SELECT a.status FROM Appointment a WHERE a.id = :id")
    Optional<Appointment.AppointmentStatus> findStatusById(@Param("id") Long id);

    @Query(nativeQuery = true, value = """
    SELECT
        i.id AS invoiceId,
        i.createdDate AS invoiceDate,
        i.status AS invoiceStatus,
        i.subtotalAmount AS subtotal,
        i.totalDiscount AS totalDiscount,
        i.finalAmount AS finalAmount,
        p.patientNo AS patientNo,
        p.firstName AS patientFirstName,
        p.lastName AS patientLastName,
        p.address AS patientAddress,
        p.city AS patientCity,
        p.mobileNo AS patientMobile,
        p.dob AS patientDob,
        a.id AS apptId,
        a.note AS apptNote,
        d.firstName AS doctorFirstName,
        d.lastName AS doctorLastName,
        s.name AS doctorSpec,
        ii.description AS itemDescription,
        it.typeName AS itemType,
        ii.amount AS itemAmount
    FROM invoices i
    JOIN patient p ON i.patient_id = p.id
    JOIN appointments a ON i.appointment_id = a.id
    LEFT JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
    LEFT JOIN doctor d ON ds.doctor_id = d.id
    LEFT JOIN specialization s ON d.specialization_id = s.id
    LEFT JOIN invoice_items ii ON i.id = ii.invoice_id
    LEFT JOIN item_types it ON ii.item_type_id = it.id
    WHERE a.id = :apptId
    ORDER BY i.id, ii.id
""")
    List<InvoiceAppointmentFlatDTO> findRawInvoicesByAppointmentId(@Param("apptId") Long apptId);

    @Query(nativeQuery = true, value = """
        SELECT
            a.id AS appointmentId,
            a.queuePosition AS queuePosition,
            a.status AS status,
            ds.scheduleDate AS scheduleDate,
            ds.startTime AS startTime,
            ds.doctorFee AS doctorFee,
            ds.clinicFee AS clinicFee,
            (ds.doctorFee + ds.clinicFee) AS totalFee,
            d.firstName AS doctorFirstName,
            d.lastName AS doctorLastName,
            CONCAT('Dr. ', d.firstName, ' ', d.lastName) AS doctorFullName,
            s.name AS specialization
        FROM appointments a
        JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
        JOIN doctor d ON ds.doctor_id = d.id
        LEFT JOIN specialization s ON d.specialization_id = s.id
        WHERE a.patient_id = :patientId
        ORDER BY ds.scheduleDate DESC, ds.startTime ASC
    """)
    List<PatientAppointmentDTO> findAppointmentsByPatientId(@Param("patientId") Long patientId);
}