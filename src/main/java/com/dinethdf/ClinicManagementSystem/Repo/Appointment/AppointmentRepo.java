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

    @Query("SELECT a FROM Appointment a WHERE a.doctorSchedule.id = :scheduleId AND a.status.code = 'ONGOING' AND a.clinicId = :clinicId")
    List<Appointment> findOngoingAppointmentsForSchedule(@Param("scheduleId") Long scheduleId, @Param("clinicId") Integer clinicId);

    @Procedure(name = "Appointment.spBookAppointment")
    Map<String, Object> sp_BookAppointment(
            @Param("p_clinic_id") Integer clinicId,
            @Param("p_createdBy") String createdBy,
            @Param("p_doctor_schedule_id") Long doctorScheduleId,
            @Param("p_patient_id") Long patientId,
            @Param("p_note") String note
    );

    // CHANGED: Native Query to get code from Lookup Table
    @Query(nativeQuery = true, value = """
        SELECT asl.code 
        FROM appointments a
        JOIN appointment_status_lookup asl ON a.statusId = asl.id
        WHERE a.id = :id AND a.clinic_id = :clinicId
    """)
    Optional<String> findStatusById(@Param("id") Long id, @Param("clinicId") Integer clinicId);

    // (findRawInvoicesByAppointmentId remains unchanged as it looks at Invoice Status, which was already fixed)
    @Query(nativeQuery = true, value = """
    SELECT
        i.id AS invoiceId,
        i.createdDate AS invoiceDate,
        isl.code AS invoiceStatusCode,
        isl.displayName AS invoiceStatusDisplay,
        isl.colorCode AS invoiceStatusColor,
        i.subtotalAmount AS subtotal,
        i.totalDiscount AS totalDiscount,
        i.finalAmount AS finalAmount,
        pm.latestDate AS paidDate,
        pm.latestMethod AS paymentMethod,
        COALESCE(pm.totalPaid, 0) AS amountPaid,
        pm.latestTxnId AS transactionId,
        p.id AS patientId,
        p.patientNo AS patientNo,
        p.firstName AS patientFirstName,
        p.lastName AS patientLastName,
        p.address AS patientAddress,
        p.city AS patientCity,
        p.mobileNo AS patientMobile,
        p.dob AS patientDob,
        a.id AS apptId,
        a.queuePosition AS queuePosition,
        a.note AS apptNote,
        ds.id AS scheduleId,
        ds.scheduleDate AS scheduleDate,
        ds.startTime AS scheduleStartTime,
        d.firstName AS doctorFirstName,
        d.lastName AS doctorLastName,
        s.name AS doctorSpec,
        ii.description AS itemDescription,
        it.typeName AS itemType,
        ii.amount AS itemAmount
    FROM appointments a
    JOIN invoices i ON a.invoice_id = i.id
    LEFT JOIN invoice_status_lookup isl ON i.statusId = isl.id
    JOIN patient p ON i.patient_id = p.id
    LEFT JOIN (
        SELECT
            p.invoice_id,
            SUM(p.amountPaid) as totalPaid,
            MAX(p.paymentTimestamp) as latestDate,
            SUBSTRING_INDEX(GROUP_CONCAT(pml.displayName ORDER BY p.paymentTimestamp DESC), ',', 1) as latestMethod,
            SUBSTRING_INDEX(GROUP_CONCAT(p.transactionId ORDER BY p.paymentTimestamp DESC), ',', 1) as latestTxnId
        FROM payments p
        LEFT JOIN payment_method_lookup pml ON p.paymentMethodId = pml.id
        LEFT JOIN payment_status_lookup psl ON p.paymentStatusId = psl.id
        WHERE psl.code = 'COMPLETED'
        GROUP BY p.invoice_id
    ) pm ON i.id = pm.invoice_id
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

    // CHANGED: Added Join to appointment_status_lookup
    @Query(nativeQuery = true, value = """
    SELECT
        a.id AS appointmentId,
        a.queuePosition AS queuePosition,
        asl.code AS status,  -- Get Code
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
    JOIN appointment_status_lookup asl ON a.statusId = asl.id -- JOIN
    JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
    JOIN doctor d ON ds.doctor_id = d.id
    LEFT JOIN specialization s ON d.specialization_id = s.id
    WHERE a.patient_id = :patientId 
      AND a.clinic_id = :clinicId
    ORDER BY 
        CASE 
            WHEN asl.code = 'PENDING_PAYMENT' THEN 1
            WHEN asl.code = 'BOOKED' THEN 2
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
        
        -- Full Status Details
        asl.code AS status,
        asl.displayName AS statusDisplayName,
        asl.colorCode AS statusColor,

        a.note AS note,
        ds.scheduleDate AS scheduleDate,
        ds.startTime AS startTime,
        CONCAT('Dr. ', d.firstName, ' ', d.lastName) AS doctorName,
        p.id AS patientId,
        p.patientNo AS patientNo,
        CONCAT(p.firstName, ' ', p.lastName) AS patientName,
        p.mobileNo AS patientMobile
    FROM appointments a
    JOIN appointment_status_lookup asl ON a.statusId = asl.id
    JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
    JOIN doctor d ON ds.doctor_id = d.id
    JOIN patient p ON a.patient_id = p.id
    WHERE
        a.clinic_id = :clinicId
        AND (:patientId IS NULL OR a.patient_id = :patientId)
        AND (:doctorId IS NULL OR d.id = :doctorId)    -- NEW: Doctor Filter
        AND (:scheduleId IS NULL OR a.doctor_schedule_id = :scheduleId)
        AND (cast(:startDate as date) IS NULL OR ds.scheduleDate >= :startDate)
        AND (cast(:endDate as date) IS NULL OR ds.scheduleDate <= :endDate)
    ORDER BY a.id DESC
""",
            countQuery = """
    SELECT COUNT(a.id)
    FROM appointments a
    JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
    JOIN doctor d ON ds.doctor_id = d.id
    WHERE
        a.clinic_id = :clinicId
        AND (:patientId IS NULL OR a.patient_id = :patientId)
        AND (:doctorId IS NULL OR d.id = :doctorId)    -- NEW: Doctor Filter
        AND (:scheduleId IS NULL OR a.doctor_schedule_id = :scheduleId)
        AND (cast(:startDate as date) IS NULL OR ds.scheduleDate >= :startDate)
        AND (cast(:endDate as date) IS NULL OR ds.scheduleDate <= :endDate)
""")
    Page<AppointmentAdminListDTO> findFilteredAppointments(
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,       // NEW
            @Param("scheduleId") Long scheduleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("clinicId") Integer clinicId,
            Pageable pageable
    );

    // (This one was already correct in your prompt, keeping it)
    @Query(nativeQuery = true, value = """
    SELECT
        a.id AS id,
        a.queuePosition AS queuePosition,
        a.note AS note,
        asl.code AS status,
        asl.displayName AS statusDisplayName,
        asl.colorCode AS statusColor,
        ds.id AS scheduleId,
        ds.scheduleDate AS scheduleDate,
        ds.startTime AS startTime,
        ds.endTime AS endTime,
        ds.doctorFee AS doctorFee,
        ds.clinicFee AS clinicFee,
        p.id AS patientId,
        CONCAT(p.firstName, ' ', p.lastName) AS patientName,
        p.patientNo AS patientNo,
        p.mobileNo AS patientMobile,
        p.nic AS patientNic,
        d.id AS doctorId,
        CONCAT('Dr. ', d.firstName, ' ', d.lastName) AS doctorName,
        s.name AS specialization,
        i.totalDiscount AS invoiceTotalDiscount,
        i.finalAmount AS invoiceFinalAmount,
        i.amountPaid AS amountPaid,
        i.balanceRemaining AS balanceRemaining,
        isl.code AS invoiceStatus,
        isl.displayName AS invoiceStatusDisplayName,
        isl.colorCode AS invoiceStatusColor,
        a.createdDate, a.createdBy,
        a.updatedDate, a.updatedBy,
        a.approvedDate, a.approvedBy
    FROM appointments a
    LEFT JOIN appointment_status_lookup asl ON a.statusId = asl.id
    JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
    JOIN doctor d ON ds.doctor_id = d.id
    LEFT JOIN specialization s ON d.specialization_id = s.id
    JOIN patient p ON a.patient_id = p.id
    LEFT JOIN invoices i ON a.invoice_id = i.id
    LEFT JOIN invoice_status_lookup isl ON i.statusId = isl.id
    WHERE a.id = :appointmentId
      AND a.clinic_id = :clinicId
""")
    Optional<AppointmentFullDetailsDTO> findAppointmentFullDetails(
            @Param("appointmentId") Long appointmentId,
            @Param("clinicId") Integer clinicId);

    // CHANGED: Added Joins to get Statuses
    @Query(nativeQuery = true, value = """
    SELECT
        a.id AS id,
        a.queuePosition AS queuePosition,
        a.note AS note,
        
        -- MAPPED STATUS
        asl.code AS status,
        asl.displayName AS statusDisplayName,
        asl.colorCode AS statusColor,
        
        ds.id AS scheduleId,
        ds.scheduleDate AS scheduleDate,
        ds.startTime AS startTime,
        ds.endTime AS endTime,
        ds.doctorFee AS doctorFee,
        ds.clinicFee AS clinicFee,
        
        p.id AS patientId,
        CONCAT(p.firstName, ' ', p.lastName) AS patientName,
        p.patientNo AS patientNo,
        p.mobileNo AS patientMobile,
        p.nic AS patientNic,
        
        d.id AS doctorId,
        CONCAT('Dr. ', d.firstName, ' ', d.lastName) AS doctorName,
        s.name AS specialization,
        
        i.totalDiscount AS invoiceTotalDiscount,
        i.finalAmount AS invoiceFinalAmount,
        i.amountPaid AS amountPaid,
        i.balanceRemaining AS balanceRemaining,
        
        -- MAPPED INVOICE STATUS
        isl.code AS invoiceStatus,
        isl.displayName AS invoiceStatusDisplayName,
        isl.colorCode AS invoiceStatusColor,

        a.createdDate, a.createdBy,
        a.updatedDate, a.updatedBy,
        a.approvedDate, a.approvedBy

    FROM appointments a
    LEFT JOIN appointment_status_lookup asl ON a.statusId = asl.id -- JOIN
    JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
    JOIN doctor d ON ds.doctor_id = d.id
    LEFT JOIN specialization s ON d.specialization_id = s.id
    JOIN patient p ON a.patient_id = p.id
    LEFT JOIN invoices i ON a.invoice_id = i.id
    LEFT JOIN invoice_status_lookup isl ON i.statusId = isl.id -- JOIN

    WHERE a.id = :appointmentId
      AND a.clinic_id = :clinicId
      AND a.patient_id = :patientId
""")
    Optional<AppointmentFullDetailsDTO> findAppointmentFullDetailsByPatientIdANDAppointmentId(
            @Param("appointmentId") Long appointmentId,
            @Param("patientId") Long patientId,
            @Param("clinicId") Integer clinicId
    );


    // CHANGED: Added Join to appointment_status_lookup
    @Query(nativeQuery = true, value = """
        SELECT
            a.id AS id,
            a.queuePosition AS queuePosition,
            asl.code AS status, -- Get Code
            a.note AS note,
            ds.scheduleDate AS scheduleDate,
            ds.startTime AS startTime,
            p.id AS patientId,
            p.patientNo AS patientNo,
            CONCAT(p.firstName, ' ', p.lastName) AS patientName,
            p.mobileNo AS patientMobile
        FROM appointments a
        JOIN appointment_status_lookup asl ON a.statusId = asl.id -- JOIN
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

    // CHANGED: Updated analytics to use the Lookup table for status strings
    @Query(nativeQuery = true, value = """
    SELECT
        COUNT(a.id) AS total,
        COALESCE(SUM(CASE WHEN asl.code = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS completed,
        COALESCE(SUM(CASE WHEN asl.code = 'BOOKED' THEN 1 ELSE 0 END), 0) AS booked,
        COALESCE(SUM(CASE WHEN asl.code = 'PENDING_PAYMENT' THEN 1 ELSE 0 END), 0) AS pendingPayment,
        COALESCE(SUM(CASE WHEN asl.code = 'CANCELLED' THEN 1 ELSE 0 END), 0) AS cancelled
    FROM appointments a
    JOIN appointment_status_lookup asl ON a.statusId = asl.id
    WHERE a.doctor_schedule_id = :scheduleId
      AND a.clinic_id = :clinicId
""")
    ScheduleAnalyticsDTO getScheduleAnalytics(
            @Param("scheduleId") Long scheduleId,
            @Param("clinicId") Integer clinicId);


    @Query(nativeQuery = true, value = """
        SELECT
            p.id AS patientId,
            a.id AS appointmentId,
            CONCAT(p.firstName, ' ', p.lastName) AS patientName,
            p.patientNo AS patientNo,
            p.nic AS nic,
            a.queuePosition AS queuePosition,
            a.createdDate AS createdDate,
            asl.code AS statusCode,
            asl.displayName AS statusDisplayName,
            asl.colorCode AS statusColor
        FROM appointments a
        JOIN appointment_status_lookup asl ON a.statusId = asl.id
        JOIN patient p ON a.patient_id = p.id
        WHERE a.doctor_schedule_id = :scheduleId
          AND p.id = :patientId
          AND a.clinic_id = :clinicId
          AND asl.code != 'CANCELLED'   -- 1. Ignore Cancelled Appointments
        ORDER BY a.id DESC              -- 2. Pick the Latest one (The new booking)
        LIMIT 1                         -- 3. Ensure strictly one result
    """)
    Optional<AppointmentCheckDTO> findDetailsByScheduleAndPatient(
            @Param("scheduleId") Long scheduleId,
            @Param("patientId") Long patientId,
            @Param("clinicId") Integer clinicId
    );

    @Procedure(name = "Appointment.cancel")
    Map<String, Object> sp_CancelAppointment(
            @Param("p_clinic_id") Integer clinicId,
            @Param("p_appointment_id") Integer appointmentId,
            @Param("p_cancel_reason") String cancelReason,
            @Param("p_cancelled_by") String cancelledBy,
            @Param("p_received_by_staff_id") Long staffId,
            @Param("p_cashier_workstation_id") Integer workstationId,
            @Param("p_refund_method") String refundMethod
    );
}