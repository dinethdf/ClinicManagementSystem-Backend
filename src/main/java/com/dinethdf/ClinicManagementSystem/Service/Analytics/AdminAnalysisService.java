package com.dinethdf.ClinicManagementSystem.Service.Analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

        String revenueSql = "SELECT COALESCE(SUM(finalAmount), 0) FROM invoices WHERE DATE(createdDate) = ?";
        summary.put("todayRevenue", jdbcTemplate.queryForObject(revenueSql, Double.class, today));


        String apptSql = """
            SELECT COUNT(*) FROM appointments a
            JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
            WHERE ds.scheduleDate = ? AND a.status != 'CANCELLED'
        """;
        summary.put("todayAppointments", jdbcTemplate.queryForObject(apptSql, Integer.class, today));

        String pendingSql = "SELECT COUNT(*) FROM appointments WHERE status = 'PENDING_PAYMENT'";
        summary.put("pendingPayments", jdbcTemplate.queryForObject(pendingSql, Integer.class));

        String patientSql = "SELECT COUNT(*) FROM patient WHERE isActive = 1";
        summary.put("activePatients", jdbcTemplate.queryForObject(patientSql, Integer.class));

        return summary;
    }

    public List<Map<String, Object>> getMonthlyRevenue(int year) {
        String sql = """
            SELECT
                MONTHNAME(createdDate) as month,
                MONTH(createdDate) as monthIndex,
                SUM(finalAmount) as totalRevenue,
                COUNT(id) as invoiceCount
            FROM invoices
            WHERE YEAR(createdDate) = ? AND status = 'PAID'
            GROUP BY MONTH(createdDate), MONTHNAME(createdDate)
            ORDER BY MONTH(createdDate)
        """;
        return jdbcTemplate.queryForList(sql, year);
    }

    public List<Map<String, Object>> getDoctorPerformanceStats() {
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
            GROUP BY d.id
            ORDER BY generatedRevenue DESC
            LIMIT 10
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getAppointmentStatusBreakdown() {
        String sql = """
            SELECT
                status,
                COUNT(*) as count,
                ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM appointments), 1) as percentage
            FROM appointments
            GROUP BY status
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getBusyDaysAnalysis() {
        String sql = """
            SELECT
                DAYNAME(ds.scheduleDate) as dayName,
                DAYOFWEEK(ds.scheduleDate) as dayIndex,
                COUNT(a.id) as visitCount
            FROM appointments a
            JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
            WHERE ds.scheduleDate >= DATE_SUB(NOW(), INTERVAL 3 MONTH)
            GROUP BY DAYOFWEEK(ds.scheduleDate), DAYNAME(ds.scheduleDate)
            ORDER BY dayIndex
        """;
        return jdbcTemplate.queryForList(sql);
    }
}
