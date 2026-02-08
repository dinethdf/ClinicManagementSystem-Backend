package com.dinethdf.ClinicManagementSystem.Controller.Financial;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Financial.*;
import com.dinethdf.ClinicManagementSystem.Model.Financial.DoctorSchedulePayment;
import com.dinethdf.ClinicManagementSystem.Model.Financial.OperationalExpense;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserPrincipal;
import com.dinethdf.ClinicManagementSystem.Service.Financial.PayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/payouts")
@RequiredArgsConstructor
public class PayoutController {

    private final PayoutService payoutService;

    @GetMapping("/doctor/search")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PayoutView>>> searchPayouts(
            @RequestParam(required = false) Integer doctorId,
            @RequestParam(required = false) DoctorSchedulePayment.PayoutStatus status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        Page<PayoutView> result = payoutService.searchPayouts(Long.valueOf(doctorId), status, startDate, endDate, page, size);
        return ResponseEntity.ok(ApiResponse.success(result, "Search Results"));
    }

    @GetMapping("/doctor/my-payouts")
    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR')")
    public ResponseEntity<ApiResponse<Page<PayoutView>>> getMyPayouts(
            @RequestParam(required = false) DoctorSchedulePayment.PayoutStatus status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {

        if (authentication.getPrincipal() instanceof String) {
            throw new AccessDeniedException("User is not authenticated correctly.");
        }
        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();

        Long doctorId = userDetails.getUserDetailsId();

        Page<PayoutView> result = payoutService.searchPayouts(doctorId, status, startDate, endDate, page, size);
        return ResponseEntity.ok(ApiResponse.success(result, "My Payout History"));
    }

    @GetMapping("/doctor/list")
    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR')")
    public ResponseEntity<ApiResponse<Page<PayoutView>>> getDoctorPayoutsList(
            @RequestParam(required = false) DoctorSchedulePayment.PayoutStatus status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<PayoutView> result = payoutService.searchPayouts(null, status, startDate, endDate, page, size);
        return ResponseEntity.ok(ApiResponse.success(result, "My Payout History"));
    }


    @PostMapping("/doctor/generate-bill/{scheduleId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<DoctorPayoutResultDTO>> generateDoctorBill(
            @PathVariable Long scheduleId, Principal p) {

        DoctorPayoutResultDTO result = payoutService.generateDoctorBill(scheduleId, p.getName());
        return ResponseEntity.ok(ApiResponse.success(result, "Bill Generated"));
    }

    @GetMapping("/doctor/pending")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<PendingBillView>>> getPendingDoctorBills(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(defaultValue = "0") int page, // Default to first page
            @RequestParam(defaultValue = "10") int size // Default 10 items
    ) {

        Page<PendingBillView> pagedData = payoutService.getPendingDoctorBills(doctorId, page, size);

        return ResponseEntity.ok(ApiResponse.success(
                pagedData,
                "Fetched Pending Bills"
        ));
    }

    @PostMapping("/doctor/pay-bulk")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<BulkPaymentResponseDTO>> payDoctors(
            @RequestBody BulkPaymentRequestDTO request, Principal p) {

        BulkPaymentResponseDTO result = payoutService.payDoctorsBulk(request, p.getName());
        return ResponseEntity.ok(ApiResponse.success(result, "Payment Successful"));
    }

    @PostMapping("/doctor/reverse/{payoutId}")
    @PreAuthorize("hasAnyAuthority('ADMIN')") // STRICTLY ADMIN ONLY
    public ResponseEntity<ApiResponse<String>> reversePayment(
            @PathVariable Integer payoutId, Principal p) {

        payoutService.reversePayment(payoutId, p.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Payment Reversed. Bill is now PENDING."));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<FinancialSummaryDTO>> getDashboardSummary(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {

        FinancialSummaryDTO summary = payoutService.getFinancialSummary(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(
                summary,
                "Financial Dashboard Stats Fetched"
        ));
    }


    @PostMapping("/expense")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<OperationalExpense>> createExpense(
            @RequestBody ExpenseCreateDTO dto, Principal p) {

        OperationalExpense result = payoutService.addExpense(dto, p.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "Expense Added"));
    }
}