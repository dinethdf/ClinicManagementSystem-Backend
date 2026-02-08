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
        Integer clinicId = TenantContext.getCurrentTenant();
        Map<String, Object> analysis = new HashMap<>();

        String attendanceSql = """
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN a.statusId = 5 THEN 1 ELSE 0 END) as completed,
            SUM(CASE WHEN a.statusId = 6 THEN 1 ELSE 0 END) as cancelled,
            SUM(CASE WHEN a.statusId = 7 THEN 1 ELSE 0 END) as noshow,
            MAX(ds.scheduleDate) as lastVisit
        FROM appointments a
        INNER JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
        WHERE a.patient_id = ? AND a.clinic_id = ?
    """;

        try {
            analysis.put("attendance", jdbcTemplate.queryForMap(attendanceSql, patientId, clinicId));
        } catch (EmptyResultDataAccessException e) {
            analysis.put("attendance", Map.of("total", 0, "completed", 0, "cancelled", 0, "noshow", 0));
        }

        // 2️⃣ Financial Stats (OPTIMIZED - uses invoice aggregates directly)
        String financialSql = """
        SELECT 
            COALESCE(SUM(finalAmount), 0) as totalBilled,
            COALESCE(SUM(amountPaid), 0) as totalPaid,
            COALESCE(SUM(balanceRemaining), 0) as totalDue
        FROM invoices
        WHERE patient_id = ? AND clinic_id = ? AND statusId != 4
    """;

        analysis.put("financials", jdbcTemplate.queryForMap(financialSql, patientId, clinicId));

        // 3️⃣ Next Appointment
        String nextApptSql = """
        SELECT 
            ds.scheduleDate, 
            ds.startTime, 
            CONCAT('Dr. ', d.firstName, ' ', d.lastName) as doctorName
        FROM appointments a
        INNER JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
        INNER JOIN doctor d ON ds.doctor_id = d.id
        WHERE a.patient_id = ? 
          AND a.clinic_id = ?
          AND ds.scheduleDate >= CURDATE()
          AND a.statusId IN (2, 3)
        ORDER BY ds.scheduleDate ASC, ds.startTime ASC
        LIMIT 1
    """;

        try {
            analysis.put("nextAppointment", jdbcTemplate.queryForMap(nextApptSql, patientId, clinicId));
        } catch (EmptyResultDataAccessException e) {
            analysis.put("nextAppointment", null);
        }

        // 4️⃣ Preferred Doctor
        String preferredDocSql = """
        SELECT 
            CONCAT('Dr. ', d.firstName, ' ', d.lastName) as doctorName,
            COUNT(*) as visitCount
        FROM appointments a
        INNER JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
        INNER JOIN doctor d ON ds.doctor_id = d.id
        WHERE a.patient_id = ? AND a.clinic_id = ? AND a.statusId = 5
        GROUP BY d.id, d.firstName, d.lastName
        ORDER BY visitCount DESC
        LIMIT 1
    """;

        try {
            analysis.put("preferredDoctor", jdbcTemplate.queryForMap(preferredDocSql, patientId, clinicId));
        } catch (EmptyResultDataAccessException e) {
            analysis.put("preferredDoctor", null);
        }

        // 5️⃣ Recent History
        String historySql = """
        SELECT
            ds.scheduleDate,
            asl.displayName as status,
            CONCAT('Dr. ', d.firstName, ' ', d.lastName) as doctorName
        FROM appointments a
        INNER JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
        INNER JOIN doctor d ON ds.doctor_id = d.id
        LEFT JOIN appointment_status_lookup asl ON a.statusId = asl.id
        WHERE a.patient_id = ? AND a.clinic_id = ?
        ORDER BY ds.scheduleDate DESC
        LIMIT 5
    """;

        analysis.put("recentHistory", jdbcTemplate.queryForList(historySql, patientId, clinicId));

        return analysis;
    }
}