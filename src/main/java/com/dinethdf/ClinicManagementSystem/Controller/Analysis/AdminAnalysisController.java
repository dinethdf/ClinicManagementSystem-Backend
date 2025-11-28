package com.dinethdf.ClinicManagementSystem.Controller.Analysis;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.Service.Analytics.AdminAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
}