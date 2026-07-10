package com.dinethdf.ClinicManagementSystem.Controller.Analysis;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleResponseDTO;
import com.dinethdf.ClinicManagementSystem.Service.Analytics.AdminAnalysisService;
import com.dinethdf.ClinicManagementSystem.Service.DoctorSchedule.DoctorScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/admin-analysis") // Updated Path
@RequiredArgsConstructor
public class AdminAnalysisController {

    private final AdminAnalysisService adminAnalysisService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardSummary() {
        return ResponseEntity.ok(ApiResponse.success(
                adminAnalysisService.getSummaryCards(),
                "Admin dashboard summary retrieved"
        ));
    }


    @GetMapping("/revenue-monthly")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMonthlyRevenue(
            @RequestParam(required = false) Integer year) {

        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        return ResponseEntity.ok(ApiResponse.success(
                adminAnalysisService.getMonthlyRevenue(targetYear),
                "Monthly revenue analysis retrieved"
        ));
    }


    @GetMapping("/doctor-performance")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDoctorPerformance() {
        return ResponseEntity.ok(ApiResponse.success(
                adminAnalysisService.getDoctorPerformanceStats(),
                "Doctor performance analysis retrieved"
        ));
    }


    @GetMapping("/appointment-status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAppointmentStatus() {
        return ResponseEntity.ok(ApiResponse.success(
                adminAnalysisService.getAppointmentStatusBreakdown(),
                "Status breakdown analysis retrieved"
        ));
    }


    @GetMapping("/busy-days")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBusyDays() {
        return ResponseEntity.ok(ApiResponse.success(
                adminAnalysisService.getBusyDaysAnalysis(),
                "Busy days analysis retrieved"
        ));
    }

    @GetMapping("/appointments/specialization-summary")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    public ResponseEntity<ApiResponse<Map<String, List<Map<String, Object>>>>> getSpecializationSummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        int targetMonth = (month != null) ? month : LocalDate.now().getMonthValue();

        return ResponseEntity.ok(ApiResponse.success(
                adminAnalysisService.getSpecializationCountsGrouped(targetYear, targetMonth),
                "Specialization-wise appointment summary retrieved"
        ));
    }

    @Autowired
    private DoctorScheduleService doctorScheduleService;

    @GetMapping("/schedules/upcoming")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<DoctorScheduleResponseDTO>>> getUpcomingSchedules(
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) String specialization,
            @PageableDefault(size = 10, sort = "scheduleDate") Pageable pageable
    ) {

        Page<DoctorScheduleResponseDTO> schedulePage = doctorScheduleService.getUpcomingSchedules(
                doctorName,
                specialization,
                null,
                pageable
        );

        return ResponseEntity.ok(ApiResponse.success(
                schedulePage,
                "Upcoming doctor schedules retrieved"
        ));
    }

    @GetMapping("/pnl")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPnL(
            @RequestParam String startDate, 
            @RequestParam String endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                adminAnalysisService.calculatePnL(startDate, endDate),
                "Profit and Loss analysis retrieved"
        ));
    }

    @GetMapping("/outstanding-invoices")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getOutstandingInvoices() {
        return ResponseEntity.ok(ApiResponse.success(
                adminAnalysisService.getOutstandingInvoices(),
                "Outstanding invoices retrieved"
        ));
    }

    @GetMapping("/discount-leakage")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDiscountLeakageReport() {
        return ResponseEntity.ok(ApiResponse.success(
                adminAnalysisService.getDiscountLeakageReport(),
                "Discount leakage report retrieved"
        ));
    }

    @GetMapping("/cashier-reconciliation")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEodCashierReport(
            @RequestParam String date, 
            @RequestParam Long staffId) {
        return ResponseEntity.ok(ApiResponse.success(
                adminAnalysisService.getEodCashierReport(date, staffId),
                "EOD Cashier reconciliation retrieved"
        ));
    }

    @GetMapping("/schedule-fill-rate")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getScheduleFillRate() {
        return ResponseEntity.ok(ApiResponse.success(
                adminAnalysisService.getScheduleFillRate(),
                "Schedule fill rate retrieved"
        ));
    }

    @GetMapping("/utilization-heatmap")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUtilizationHeatmap() {
        return ResponseEntity.ok(ApiResponse.success(
                adminAnalysisService.getUtilizationHeatmap(),
                "Utilization heatmap retrieved"
        ));
    }

    @GetMapping("/wait-time")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getWaitTimeAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(
                adminAnalysisService.getWaitTimeAnalytics(),
                "Wait time analytics retrieved"
        ));
    }

    @GetMapping("/punctuality")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDoctorPunctualityStats() {
        return ResponseEntity.ok(ApiResponse.success(
                adminAnalysisService.getDoctorPunctualityStats(),
                "Doctor punctuality stats retrieved"
        ));
    }
}