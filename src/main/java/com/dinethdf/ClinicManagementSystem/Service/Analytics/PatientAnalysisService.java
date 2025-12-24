package com.dinethdf.ClinicManagementSystem.Service.Analytics;

import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PatientAnalysisService {

    private final JdbcTemplate jdbcTemplate;

    public Map<String, Object> getPatientAnalysis(Long patientId) {

        Map<String, Object> analysis = new HashMap<>();
        Integer clinicId = TenantContext.getCurrentTenant();

        String attendanceSql = """
            SELECT 
                COUNT(*) as total,
                COALESCE(SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END), 0) as completed,
                COALESCE(SUM(CASE WHEN a.status = 'CANCELLED' THEN 1 ELSE 0 END), 0) as cancelled,
                COALESCE(SUM(CASE WHEN a.status = 'NOSHOW' THEN 1 ELSE 0 END), 0) as noshow,
                MAX(ds.scheduleDate) as lastVisit
            FROM appointments a
            JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
            WHERE a.patient_id = ? AND a.clinic_id = ?
        """;

        try {
            Map<String, Object> attendanceStats = jdbcTemplate.queryForMap(attendanceSql, patientId, clinicId);
            analysis.put("attendance", attendanceStats);
        } catch (EmptyResultDataAccessException e) {
            analysis.put("attendance", null);
        }

        // 2. FINANCIAL SUMMARY (Updated & Fixed)
        // We use two separate queries to avoid join multiplication issues

        // A. Total Billed (From Invoices)
        // COALESCE ensures this returns 0.0 instead of null if no records exist
        String billedSql = """
            SELECT COALESCE(SUM(finalAmount), 0) 
            FROM invoices 
            WHERE patient_id = ? AND clinic_id = ? AND status != 'CANCELLED'
        """;
        Double totalBilled = jdbcTemplate.queryForObject(billedSql, Double.class, patientId, clinicId);

        // B. Total Paid (From Payments)
        String paidSql = """
            SELECT COALESCE(SUM(p.amountPaid), 0) 
            FROM payments p
            JOIN invoices i ON p.invoice_id = i.id
            WHERE i.patient_id = ? AND i.clinic_id = ? 
              AND p.paymentStatus = 'COMPLETED'
        """;
        Double totalPaid = jdbcTemplate.queryForObject(paidSql, Double.class, patientId, clinicId);

        // C. Calculate Due
        Double totalDue = totalBilled - totalPaid;

        Map<String, Object> financeStats = new HashMap<>();
        financeStats.put("totalBilled", totalBilled);
        financeStats.put("totalPaid", totalPaid);
        financeStats.put("totalDue", totalDue);

        analysis.put("financials", financeStats);


        // 3. NEXT APPOINTMENT
        // This DOES need try-catch because if no rows are found, queryForMap throws exception
        String nextApptSql = """
            SELECT ds.scheduleDate, ds.startTime, CONCAT('Dr. ', d.firstName, ' ', d.lastName) as doctorName
            FROM appointments a
            JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
            JOIN doctor d ON ds.doctor_id = d.id
            WHERE a.patient_id = ? 
              AND a.clinic_id = ?
              AND ds.scheduleDate >= CURDATE()
              AND a.status IN ('BOOKED', 'PENDING_PAYMENT')
            ORDER BY ds.scheduleDate ASC, ds.startTime ASC
            LIMIT 1
        """;

        try {
            Map<String, Object> nextAppt = jdbcTemplate.queryForMap(nextApptSql, patientId, clinicId);
            analysis.put("nextAppointment", nextAppt);
        } catch (EmptyResultDataAccessException e) {
            analysis.put("nextAppointment", null);
        }

        // 4. PREFERRED DOCTOR
        String favDocSql = """
            SELECT 
                CONCAT('Dr. ', d.firstName, ' ', d.lastName) as doctorName,
                COUNT(*) as visitCount
            FROM appointments a
            JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
            JOIN doctor d ON ds.doctor_id = d.id
            WHERE a.patient_id = ? AND a.clinic_id = ? AND a.status = 'COMPLETED'
            GROUP BY d.id
            ORDER BY visitCount DESC
            LIMIT 1
        """;

        try {
            Map<String, Object> favDoc = jdbcTemplate.queryForMap(favDocSql, patientId, clinicId);
            analysis.put("preferredDoctor", favDoc);
        } catch (EmptyResultDataAccessException e) {
            analysis.put("preferredDoctor", null);
        }

        // 5. RECENT HISTORY
        // queryForList returns empty list [] if no rows, so no try-catch needed
        String historySql = """
            SELECT 
                ds.scheduleDate, 
                a.status, 
                CONCAT('Dr. ', d.firstName, ' ', d.lastName) as doctorName
            FROM appointments a
            JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
            JOIN doctor d ON ds.doctor_id = d.id
            WHERE a.patient_id = ? AND a.clinic_id = ?
            ORDER BY ds.scheduleDate DESC
            LIMIT 5
        """;

        List<Map<String, Object>> history = jdbcTemplate.queryForList(historySql, patientId, clinicId);
        analysis.put("recentHistory", history);

        return analysis;
    }
}