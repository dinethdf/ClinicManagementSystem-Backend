package com.dinethdf.ClinicManagementSystem.Repo.Appointment;

import com.dinethdf.ClinicManagementSystem.DTO.Appointment.*;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.ScheduleAnalyticsDTO;
import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface AppointmentRepo extends JpaRepository<Appointment, Long> {

    @Procedure(name = "Appointment.spBookAppointment")
    Map<String, Object> sp_BookAppointment(
            @Param("p_clinic_id") Integer clinicId, // <--- Added Clinic ID
            @Param("p_createdBy") String createdBy,
            @Param("p_doctor_schedule_id") Long doctorScheduleId,
            @Param("p_patient_id") Long patientId,
            @Param("p_note") String note
    );

    @Query("SELECT a.status FROM Appointment a WHERE a.id = :id AND a.clinicId = :clinicId")
    Optional<Appointment.AppointmentStatus> findStatusById(@Param("id") Long id, @Param("clinicId") Integer clinicId);

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
    FROM appointments a
    JOIN invoices i ON a.invoice_id = i.id
    JOIN patient p ON i.patient_id = p.id
    LEFT JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
    LEFT JOIN doctor d ON ds.doctor_id = d.id
    LEFT JOIN specialization s ON d.specialization_id = s.id
    LEFT JOIN invoice_items ii ON i.id = ii.invoice_id
    LEFT JOIN item_types it ON ii.item_type_id = it.id
    WHERE a.id = :apptId 
      AND a.clinic_id = :clinicId
    ORDER BY i.id, ii.id
""")
    List<InvoiceAppointmentFlatDTO> findRawInvoicesByAppointmentId(
            @Param("apptId") Long apptId,
            @Param("clinicId") Integer clinicId);

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
      AND a.clinic_id = :clinicId
    ORDER BY 
        CASE 
            WHEN a.status = 'PENDING_PAYMENT' THEN 1
            WHEN a.status = 'BOOKED' THEN 2
            ELSE 3
        END ASC,
        ds.scheduleDate DESC, 
        ds.startTime ASC
""")
    List<PatientAppointmentDTO> findAppointmentsByPatientId(
            @Param("patientId") Long patientId,
            @Param("clinicId") Integer clinicId);

    @Query(nativeQuery = true, value = """
    SELECT
        a.id AS id,
        a.queuePosition AS queuePosition,
        a.status AS status,
        a.note AS note,
        ds.scheduleDate AS scheduleDate,
        ds.startTime AS startTime,
        CONCAT('Dr. ', d.firstName, ' ', d.lastName) AS doctorName,
        p.id AS patientId,
        p.patientNo AS patientNo,
        CONCAT(p.firstName, ' ', p.lastName) AS patientName,
        p.mobileNo AS patientMobile
    FROM appointments a
    JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
    JOIN doctor d ON ds.doctor_id = d.id
    JOIN patient p ON a.patient_id = p.id
    WHERE
        a.clinic_id = :clinicId
        AND (:patientId IS NULL OR a.patient_id = :patientId)
        AND (:scheduleId IS NULL OR a.doctor_schedule_id = :scheduleId)
        AND (cast(:startDate as date) IS NULL OR ds.scheduleDate >= :startDate)
        AND (cast(:endDate as date) IS NULL OR ds.scheduleDate <= :endDate)
    ORDER BY a.id DESC
""",
            countQuery = """
    SELECT COUNT(a.id)
    FROM appointments a
    JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
    WHERE
        a.clinic_id = :clinicId
        AND (:patientId IS NULL OR a.patient_id = :patientId)
        AND (:scheduleId IS NULL OR a.doctor_schedule_id = :scheduleId)
        AND (cast(:startDate as date) IS NULL OR ds.scheduleDate >= :startDate)
        AND (cast(:endDate as date) IS NULL OR ds.scheduleDate <= :endDate)
""")
    Page<AppointmentAdminListDTO> findFilteredAppointments(
            @Param("patientId") Long patientId,
            @Param("scheduleId") Long scheduleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("clinicId") Integer clinicId,
            Pageable pageable
    );

    @Query(nativeQuery = true, value = """
    SELECT
        a.id AS id,
        a.queuePosition AS queuePosition,
        a.note AS note,
        a.status AS status,
        
        -- Schedule Details
        ds.id AS scheduleId,
        ds.scheduleDate AS scheduleDate,
        ds.startTime AS startTime,
        ds.endTime AS endTime,
        ds.doctorFee AS doctorFee,
        ds.clinicFee AS clinicFee,
        
        -- Patient Details
        p.id AS patientId,
        CONCAT(p.firstName, ' ', p.lastName) AS patientName,
        p.patientNo AS patientNo,
        p.mobileNo AS patientMobile,
        p.nic AS patientNic,
        
        -- Doctor Details
        d.id AS doctorId,
        CONCAT('Dr. ', d.firstName, ' ', d.lastName) AS doctorName,
        s.name AS specialization,
        
        -- Invoice Details
        i.totalDiscount AS invoiceTotalDiscount,
        i.finalAmount AS invoiceFinalAmount,
        i.status AS invoiceStatus,

        -- System Columns
        a.createdDate, a.createdBy,
        a.updatedDate, a.updatedBy,
        a.approvedDate, a.approvedBy

    FROM appointments a
    JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
    JOIN doctor d ON ds.doctor_id = d.id
    LEFT JOIN specialization s ON d.specialization_id = s.id
    JOIN patient p ON a.patient_id = p.id
    LEFT JOIN invoices i ON a.invoice_id = i.id

    WHERE a.id = :appointmentId
      AND a.clinic_id = :clinicId
""")
    Optional<AppointmentFullDetailsDTO> findAppointmentFullDetails(
            @Param("appointmentId") Long appointmentId,
            @Param("clinicId") Integer clinicId);

    @Query(nativeQuery = true, value = """
    SELECT
        a.id AS id,
        a.queuePosition AS queuePosition,
        a.note AS note,
        a.status AS status,
        
        -- Schedule Details
        ds.id AS scheduleId,
        ds.scheduleDate AS scheduleDate,
        ds.startTime AS startTime,
        ds.endTime AS endTime,
        ds.doctorFee AS doctorFee,
        ds.clinicFee AS clinicFee,
        
        -- Patient Details
        p.id AS patientId,
        CONCAT(p.firstName, ' ', p.lastName) AS patientName,
        p.patientNo AS patientNo,
        p.mobileNo AS patientMobile,
        p.nic AS patientNic,
        
        -- Doctor Details
        d.id AS doctorId,
        CONCAT('Dr. ', d.firstName, ' ', d.lastName) AS doctorName,
        s.name AS specialization,
        
        -- Invoice Details
        i.totalDiscount AS invoiceTotalDiscount,
        i.finalAmount AS invoiceFinalAmount,
        i.status AS invoiceStatus,

        -- System Columns
        a.createdDate, a.createdBy,
        a.updatedDate, a.updatedBy,
        a.approvedDate, a.approvedBy

    FROM appointments a
    JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
    JOIN doctor d ON ds.doctor_id = d.id
    LEFT JOIN specialization s ON d.specialization_id = s.id
    JOIN patient p ON a.patient_id = p.id
    LEFT JOIN invoices i ON a.invoice_id = i.id

    WHERE a.id = :appointmentId
      AND a.clinic_id = :clinicId
      AND a.patient_id = :patientId
""")
    Optional<AppointmentFullDetailsDTO> findAppointmentFullDetailsByPatientIdANDAppointmentId(
            @Param("appointmentId") Long appointmentId,
            @Param("patientId") Long patientId,
            @Param("clinicId") Integer clinicId
    );


    @Query(nativeQuery = true, value = """
        SELECT
            a.id AS id,
            a.queuePosition AS queuePosition,
            a.status AS status,
            a.note AS note,
            ds.scheduleDate AS scheduleDate,
            ds.startTime AS startTime,
            p.id AS patientId,
            p.patientNo AS patientNo,
            CONCAT(p.firstName, ' ', p.lastName) AS patientName,
            p.mobileNo AS patientMobile
        FROM appointments a
        JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
        JOIN patient p ON a.patient_id = p.id
        WHERE a.doctor_schedule_id = :scheduleId
          AND a.clinic_id = :clinicId
        ORDER BY a.queuePosition ASC
    """,
            countQuery = """
        SELECT COUNT(a.id)
        FROM appointments a
        WHERE a.doctor_schedule_id = :scheduleId
          AND a.clinic_id = :clinicId
    """)
    Page<AppointmentByScheduleListDTO> findAppointmentsByScheduleId(
            @Param("scheduleId") Long scheduleId,
            @Param("clinicId") Integer clinicId,
            Pageable pageable
    );

    @Query(nativeQuery = true, value = """
    SELECT
        COUNT(id) AS total,
        COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS completed,
        COALESCE(SUM(CASE WHEN status = 'BOOKED' THEN 1 ELSE 0 END), 0) AS booked,
        COALESCE(SUM(CASE WHEN status = 'PENDING_PAYMENT' THEN 1 ELSE 0 END), 0) AS pendingPayment,
        COALESCE(SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END), 0) AS cancelled
    FROM appointments
    WHERE doctor_schedule_id = :scheduleId
      AND clinic_id = :clinicId
""")
    ScheduleAnalyticsDTO getScheduleAnalytics(
            @Param("scheduleId") Long scheduleId,
            @Param("clinicId") Integer clinicId);


    @Query("""
        SELECT new com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentCheckDTO(
            p.Id,
            a.Id,
            CONCAT(p.firstName, ' ', p.lastName),
            p.patientNo,
            p.nic,
            a.queuePosition,
            CAST(a.status AS string),
            a.createdDate
        )
        FROM Appointment a
        JOIN a.patient p
        WHERE a.doctorSchedule.id = :scheduleId
          AND p.id = :patientId
          AND a.clinicId = :clinicId
    """)
    Optional<AppointmentCheckDTO> findDetailsByScheduleAndPatient(
            @Param("scheduleId") Long scheduleId,
            @Param("patientId") Long patientId,
            @Param("clinicId") Integer clinicId
    );

    @Procedure(name = "Appointment.cancel")
    String sp_CancelAppointment(
            @Param("p_clinic_id") Integer clinicId,
            @Param("p_appointment_id") Integer appointmentId,
            @Param("p_cancel_reason") String cancelReason,
            @Param("p_cancelled_by") String cancelledBy
    );
}