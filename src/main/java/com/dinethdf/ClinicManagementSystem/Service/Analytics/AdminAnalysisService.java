package com.dinethdf.ClinicManagementSystem.Service.Analytics;

import com.dinethdf.ClinicManagementSystem.config.TenantContext; // <--- Import Context
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAnalysisService {

    private final JdbcTemplate jdbcTemplate;

    public Map<String, Object> getSummaryCards() {
        Map<String, Object> summary = new HashMap<>();
        String today = LocalDate.now().toString();
        Integer clinicId = TenantContext.getCurrentTenant(); // <--- Get Tenant


        String revenueSql = "SELECT COALESCE(SUM(finalAmount), 0) FROM invoices WHERE DATE(createdDate) = ? AND clinic_id = ?";
        summary.put("todayRevenue", jdbcTemplate.queryForObject(revenueSql, Double.class, today, clinicId));

        String apptSql = """
            SELECT COUNT(*) FROM appointments a
            JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
            WHERE ds.scheduleDate = ? 
              AND a.status != 'CANCELLED'
              AND a.clinic_id = ? 
        """;
        summary.put("todayAppointments", jdbcTemplate.queryForObject(apptSql, Integer.class, today, clinicId));

        String pendingSql = "SELECT COUNT(*) FROM appointments WHERE status = 'PENDING_PAYMENT' AND clinic_id = ?";
        summary.put("pendingPayments", jdbcTemplate.queryForObject(pendingSql, Integer.class, clinicId));

        String patientSql = "SELECT COUNT(*) FROM patient WHERE isActive = 1 AND clinic_id = ?";
        summary.put("activePatients", jdbcTemplate.queryForObject(patientSql, Integer.class, clinicId));

        return summary;
    }

    public List<Map<String, Object>> getMonthlyRevenue(int year) {
        Integer clinicId = TenantContext.getCurrentTenant();
        String sql = """
            SELECT
                MONTHNAME(createdDate) as month,
                MONTH(createdDate) as monthIndex,
                SUM(finalAmount) as totalRevenue,
                COUNT(id) as invoiceCount
            FROM invoices
            WHERE YEAR(createdDate) = ? 
              AND status = 'PAID'
              AND clinic_id = ? 
            GROUP BY MONTH(createdDate), MONTHNAME(createdDate)
            ORDER BY MONTH(createdDate)
        """;
        return jdbcTemplate.queryForList(sql, year, clinicId);
    }

    public List<Map<String, Object>> getDoctorPerformanceStats() {
        Integer clinicId = TenantContext.getCurrentTenant();
        String sql = """
            SELECT
                CONCAT('Dr. ', d.firstName, ' ', d.lastName) as doctorName,
                s.name as specialization,
                COUNT(a.id) as totalAppointments,
                SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedCount,
                SUM(CASE WHEN a.status IN ('COMPLETED', 'Booked') THEN (ds.doctorFee + ds.clinicFee) ELSE 0 END) as generatedRevenue
            FROM doctor d
            JOIN doctorSchedule ds ON d.id = ds.doctor_id
            JOIN appointments a ON ds.id = a.doctor_schedule_id
            LEFT JOIN specialization s ON d.specialization_id = s.id
            WHERE a.status != 'CANCELLED'
              AND d.clinic_id = ?
            GROUP BY d.id
            ORDER BY generatedRevenue DESC
            LIMIT 10
        """;
        return jdbcTemplate.queryForList(sql, clinicId);
    }

    public List<Map<String, Object>> getAppointmentStatusBreakdown() {
        Integer clinicId = TenantContext.getCurrentTenant();
        String sql = """
            SELECT
                status,
                COUNT(*) as count,
                ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM appointments WHERE clinic_id = ?), 1) as percentage
            FROM appointments
            WHERE clinic_id = ?
            GROUP BY status
        """;
        return jdbcTemplate.queryForList(sql, clinicId, clinicId);
    }

    public List<Map<String, Object>> getBusyDaysAnalysis() {
        Integer clinicId = TenantContext.getCurrentTenant();
        String sql = """
            SELECT
                DAYNAME(ds.scheduleDate) as dayName,
                DAYOFWEEK(ds.scheduleDate) as dayIndex,
                COUNT(a.id) as visitCount
            FROM appointments a
            JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
            WHERE ds.scheduleDate >= DATE_SUB(NOW(), INTERVAL 3 MONTH)
              AND a.clinic_id = ?
            GROUP BY DAYOFWEEK(ds.scheduleDate), DAYNAME(ds.scheduleDate)
            ORDER BY dayIndex
        """;
        return jdbcTemplate.queryForList(sql, clinicId);
    }

    public Map<String, List<Map<String, Object>>> getSpecializationCountsGrouped(int year, int month) {
        Integer clinicId = TenantContext.getCurrentTenant();
        String sql = """
        (
            SELECT
                'year' AS rangeType,
                s.name AS specialization,
                COUNT(a.id) AS appointmentCount
            FROM appointments a
            JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
            JOIN doctor d ON ds.doctor_id = d.id
            JOIN specialization s ON d.specialization_id = s.id
            WHERE YEAR(a.createdDate) = ? 
              AND a.clinic_id = ?
            GROUP BY s.name
        )
        UNION ALL
        (
            SELECT
                'month' AS rangeType,
                s.name AS specialization,
                COUNT(a.id) AS appointmentCount
            FROM appointments a
            JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
            JOIN doctor d ON ds.doctor_id = d.id
            JOIN specialization s ON d.specialization_id = s.id
            WHERE YEAR(a.createdDate) = ? AND MONTH(a.createdDate) = ?
              AND a.clinic_id = ?
            GROUP BY s.name
        )
        UNION ALL
        (
            SELECT
                'week' AS rangeType,
                s.name AS specialization,
                COUNT(a.id) AS appointmentCount
            FROM appointments a
            JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
            JOIN doctor d ON ds.doctor_id = d.id
            JOIN specialization s ON d.specialization_id = s.id
            WHERE YEARWEEK(a.createdDate, 1) = YEARWEEK(CURDATE(), 1)
              AND a.clinic_id = ?
            GROUP BY s.name
        )
        ORDER BY rangeType, specialization
        """;

        // Arguments order:
        // 1. Year (param 1)
        // 2. ClinicID (param 2)
        // 3. Year (param 3)
        // 4. Month (param 4)
        // 5. ClinicID (param 5)
        // 6. ClinicID (param 6)
        List<Map<String, Object>> queryResult = jdbcTemplate.queryForList(sql, year, clinicId, year, month, clinicId, clinicId);

        // Transform into grouped response
        Map<String, List<Map<String, Object>>> groupedResult = new HashMap<>();
        groupedResult.put("year", new ArrayList<>());
        groupedResult.put("month", new ArrayList<>());
        groupedResult.put("week", new ArrayList<>());

        for (Map<String, Object> row : queryResult) {
            String rangeType = ((String) row.get("rangeType")).toLowerCase();
            Map<String, Object> data = new HashMap<>();
            data.put("specialization", row.get("specialization"));
            data.put("appointmentCount", row.get("appointmentCount"));

            groupedResult.computeIfAbsent(rangeType, k -> new ArrayList<>()).add(data);
        }

        return groupedResult;
    }
}