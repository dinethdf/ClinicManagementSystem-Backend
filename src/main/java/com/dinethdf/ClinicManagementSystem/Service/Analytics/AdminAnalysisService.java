package com.dinethdf.ClinicManagementSystem.Service.Analytics;

import com.dinethdf.ClinicManagementSystem.config.TenantContext;
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
        Integer clinicId = TenantContext.getCurrentTenant();

        String revenueSql = "SELECT COALESCE(SUM(finalAmount), 0) FROM invoices WHERE DATE(createdDate) = ? AND clinic_id = ?";
        summary.put("todayRevenue", jdbcTemplate.queryForObject(revenueSql, Double.class, today, clinicId));

        String apptSql = """
            SELECT COUNT(*) FROM appointments a
            JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id
            JOIN appointment_status_lookup asl ON a.statusId = asl.id
            WHERE ds.scheduleDate = ? 
              AND asl.code != 'CANCELLED'
              AND a.clinic_id = ? 
        """;
        summary.put("todayAppointments", jdbcTemplate.queryForObject(apptSql, Integer.class, today, clinicId));

        String pendingSql = """
            SELECT COUNT(*) FROM appointments a
            JOIN appointment_status_lookup asl ON a.statusId = asl.id
            WHERE asl.code IN ('PENDING_PAYMENT', 'PARTIAL') 
              AND a.clinic_id = ?
        """;
        summary.put("pendingPayments", jdbcTemplate.queryForObject(pendingSql, Integer.class, clinicId));

        String patientSql = "SELECT COUNT(*) FROM patient WHERE isActive = 1 AND clinic_id = ?";
        summary.put("activePatients", jdbcTemplate.queryForObject(patientSql, Integer.class, clinicId));

        return summary;
    }

    public List<Map<String, Object>> getMonthlyRevenue(int year) {
        Integer clinicId = TenantContext.getCurrentTenant();
        String sql = """
            SELECT
                MONTHNAME(i.createdDate) as month,
                MONTH(i.createdDate) as monthIndex,
                SUM(i.finalAmount) as totalRevenue,
                SUM(CASE WHEN it.typeName = 'DOCTOR_FEE' THEN ii.amount ELSE 0 END) as doctorFees,
                SUM(CASE WHEN it.typeName = 'CLINIC_FEE' THEN ii.amount ELSE 0 END) as clinicFees,
                COUNT(DISTINCT i.id) as invoiceCount
            FROM invoices i
            JOIN invoice_status_lookup isl ON i.statusId = isl.id
            LEFT JOIN invoice_items ii ON i.id = ii.invoice_id
            LEFT JOIN item_types it ON ii.item_type_id = it.id
            WHERE YEAR(i.createdDate) = ? 
              AND isl.code IN ('PAID', 'PARTIAL')
              AND i.clinic_id = ? 
            GROUP BY MONTH(i.createdDate), MONTHNAME(i.createdDate)
            ORDER BY MONTH(i.createdDate)
        """;
        return jdbcTemplate.queryForList(sql, year, clinicId);
    }

    public Map<String, Object> calculatePnL(String startDate, String endDate) {
        Integer clinicId = TenantContext.getCurrentTenant();
        
        String revenueSql = "SELECT COALESCE(SUM(finalAmount), 0) FROM invoices i JOIN invoice_status_lookup isl ON i.statusId = isl.id WHERE isl.code = 'PAID' AND DATE(i.createdDate) BETWEEN ? AND ? AND i.clinic_id = ?";
        Double totalRevenue = jdbcTemplate.queryForObject(revenueSql, Double.class, startDate, endDate, clinicId);
        
        String doctorPayoutSql = "SELECT COALESCE(SUM(amount), 0) FROM payout_transactions WHERE payoutType = 'DOCTOR' AND DATE(transactionDate) BETWEEN ? AND ? AND clinic_id = ?";
        Double doctorPayouts = 0.0;
        try {
            doctorPayouts = jdbcTemplate.queryForObject(doctorPayoutSql, Double.class, startDate, endDate, clinicId);
        } catch(Exception e) {} // Table might be empty or missing column
        
        String opsExpenseSql = "SELECT COALESCE(SUM(amount), 0) FROM operational_expenses WHERE DATE(expenseDate) BETWEEN ? AND ? AND clinic_id = ?";
        Double opsExpenses = 0.0;
        try {
            opsExpenses = jdbcTemplate.queryForObject(opsExpenseSql, Double.class, startDate, endDate, clinicId);
        } catch(Exception e) {}

        if(totalRevenue == null) totalRevenue = 0.0;
        if(doctorPayouts == null) doctorPayouts = 0.0;
        if(opsExpenses == null) opsExpenses = 0.0;
        
        Map<String, Object> pnl = new HashMap<>();
        pnl.put("totalRevenue", totalRevenue);
        pnl.put("doctorPayouts", doctorPayouts);
        pnl.put("opsExpenses", opsExpenses);
        pnl.put("netProfit", totalRevenue - doctorPayouts - opsExpenses);
        
        return pnl;
    }

    public List<Map<String, Object>> getOutstandingInvoices() {
        Integer clinicId = TenantContext.getCurrentTenant();
        String sql = """
            SELECT
                i.id as invoiceId,
                CONCAT(p.firstName, ' ', p.lastName) as patientName,
                p.mobileNo1 as contact,
                i.finalAmount as totalAmount,
                i.balanceRemaining,
                i.dueDate,
                isl.displayName as status
            FROM invoices i
            JOIN patient p ON i.patient_id = p.id
            JOIN invoice_status_lookup isl ON i.statusId = isl.id
            WHERE i.balanceRemaining > 0
              AND i.clinic_id = ?
            ORDER BY i.dueDate ASC
        """;
        return jdbcTemplate.queryForList(sql, clinicId);
    }

    public List<Map<String, Object>> getDiscountLeakageReport() {
        Integer clinicId = TenantContext.getCurrentTenant();
        String sql = """
            SELECT 
                it.typeName as discountType,
                i.createdBy as authorizedBy,
                SUM(ii.amount) as totalDiscountGiven,
                COUNT(ii.id) as timesGiven
            FROM invoice_items ii
            JOIN item_types it ON ii.item_type_id = it.id
            JOIN invoices i ON ii.invoice_id = i.id
            WHERE it.typeName IN ('DOCTOR_DISCOUNT', 'CLINIC_DISCOUNT')
              AND i.clinic_id = ?
            GROUP BY it.typeName, i.createdBy
            ORDER BY totalDiscountGiven DESC
        """;
        return jdbcTemplate.queryForList(sql, clinicId);
    }

    public Map<String, Object> getEodCashierReport(String date, Long staffId) {
        Integer clinicId = TenantContext.getCurrentTenant();
        String sql = """
            SELECT 
                i.cashierWorkstationId,
                COUNT(i.id) as totalTransactions,
                SUM(i.finalAmount) as totalCollected
            FROM invoices i
            WHERE DATE(i.createdDate) = ? 
              AND i.receivedByStaffId = ?
              AND i.clinic_id = ?
            GROUP BY i.cashierWorkstationId
        """;
        List<Map<String, Object>> stats = jdbcTemplate.queryForList(sql, date, staffId, clinicId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("date", date);
        response.put("staffId", staffId);
        response.put("workstations", stats);
        return response;
    }

    public List<Map<String, Object>> getScheduleFillRate() {
        Integer clinicId = TenantContext.getCurrentTenant();
        String sql = """
            SELECT 
                ds.id as scheduleId,
                CONCAT('Dr. ', d.firstName, ' ', d.lastName) as doctorName,
                ds.scheduleDate,
                ds.currentPatients,
                ds.maxPatients,
                ROUND((ds.currentPatients * 100.0) / ds.maxPatients, 1) as fillPercentage
            FROM doctorSchedule ds
            JOIN doctor d ON ds.doctor_id = d.id
            WHERE ds.scheduleDate >= CURDATE()
              AND d.clinic_id = ?
            ORDER BY ds.scheduleDate ASC
        """;
        return jdbcTemplate.queryForList(sql, clinicId);
    }

    public List<Map<String, Object>> getUtilizationHeatmap() {
        Integer clinicId = TenantContext.getCurrentTenant();
        String sql = """
            SELECT 
                DAYNAME(a.createdDate) as dayOfWeek,
                HOUR(a.createdDate) as hourOfDay,
                COUNT(a.id) as appointmentCount
            FROM appointments a
            WHERE a.clinic_id = ?
              AND a.createdDate >= DATE_SUB(NOW(), INTERVAL 1 MONTH)
            GROUP BY DAYNAME(a.createdDate), HOUR(a.createdDate)
            ORDER BY DAYOFWEEK(a.createdDate), HOUR(a.createdDate)
        """;
        return jdbcTemplate.queryForList(sql, clinicId);
    }

    public List<Map<String, Object>> getDoctorPunctualityStats() {
        Integer clinicId = TenantContext.getCurrentTenant();
        String sql = """
            SELECT 
                CONCAT('Dr. ', d.firstName, ' ', d.lastName) as doctorName,
                0 as averageDelayMinutes,
                COUNT(ds.id) as totalSchedules
            FROM doctor d
            JOIN doctorSchedule ds ON d.id = ds.doctor_id
            WHERE d.clinic_id = ?
            GROUP BY d.id
        """;
        return jdbcTemplate.queryForList(sql, clinicId);
    }

    public List<Map<String, Object>> getWaitTimeAnalytics() {
        Integer clinicId = TenantContext.getCurrentTenant();
        String sql = """
            SELECT 
                DAYNAME(a.createdDate) as dayName,
                AVG(TIMESTAMPDIFF(MINUTE, a.createdDate, a.consultationStartTime)) as avgWaitTimeMinutes
            FROM appointments a
            WHERE a.consultationStartTime IS NOT NULL
              AND a.clinic_id = ?
            GROUP BY DAYNAME(a.createdDate)
        """;
        return jdbcTemplate.queryForList(sql, clinicId);
    }

    public List<Map<String, Object>> getDoctorPerformanceStats() {
        Integer clinicId = TenantContext.getCurrentTenant();
        String sql = """
            SELECT
                CONCAT('Dr. ', d.firstName, ' ', d.lastName) as doctorName,
                s.name as specialization,
                COUNT(a.id) as totalAppointments,
                SUM(CASE WHEN asl.code = 'COMPLETED' THEN 1 ELSE 0 END) as completedCount,
                SUM(CASE WHEN asl.code IN ('COMPLETED', 'BOOKED') THEN (ds.doctorFee + ds.clinicFee) ELSE 0 END) as generatedRevenue
            FROM doctor d
            JOIN doctorSchedule ds ON d.id = ds.doctor_id
            JOIN appointments a ON ds.id = a.doctor_schedule_id
            JOIN appointment_status_lookup asl ON a.statusId = asl.id
            LEFT JOIN specialization s ON d.specialization_id = s.id
            WHERE asl.code != 'CANCELLED'
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
                asl.displayName as status,
                asl.colorCode as color,
                COUNT(*) as count,
                ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM appointments WHERE clinic_id = ?), 1) as percentage
            FROM appointments a
            JOIN appointment_status_lookup asl ON a.statusId = asl.id
            WHERE a.clinic_id = ?
            GROUP BY asl.displayName, asl.colorCode
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
            SELECT 'year' AS rangeType, s.name AS specialization, COUNT(a.id) AS appointmentCount
            FROM appointments a JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id JOIN doctor d ON ds.doctor_id = d.id JOIN specialization s ON d.specialization_id = s.id
            WHERE YEAR(a.createdDate) = ? AND a.clinic_id = ? GROUP BY s.name
        ) UNION ALL (
            SELECT 'month' AS rangeType, s.name AS specialization, COUNT(a.id) AS appointmentCount
            FROM appointments a JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id JOIN doctor d ON ds.doctor_id = d.id JOIN specialization s ON d.specialization_id = s.id
            WHERE YEAR(a.createdDate) = ? AND MONTH(a.createdDate) = ? AND a.clinic_id = ? GROUP BY s.name
        ) UNION ALL (
            SELECT 'week' AS rangeType, s.name AS specialization, COUNT(a.id) AS appointmentCount
            FROM appointments a JOIN doctorSchedule ds ON a.doctor_schedule_id = ds.id JOIN doctor d ON ds.doctor_id = d.id JOIN specialization s ON d.specialization_id = s.id
            WHERE YEARWEEK(a.createdDate, 1) = YEARWEEK(CURDATE(), 1) AND a.clinic_id = ? GROUP BY s.name
        ) ORDER BY rangeType, specialization
        """;
        List<Map<String, Object>> queryResult = jdbcTemplate.queryForList(sql, year, clinicId, year, month, clinicId, clinicId);
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