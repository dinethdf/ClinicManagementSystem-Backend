package com.dinethdf.ClinicManagementSystem.Controller.Payout;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Financial.*;
import com.dinethdf.ClinicManagementSystem.Model.Financial.DoctorSchedulePayment;
import com.dinethdf.ClinicManagementSystem.Model.Financial.OperationalExpense;
import com.dinethdf.ClinicManagementSystem.Service.Payout.PayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("api/payouts")
@RequiredArgsConstructor
public class PayoutController {

    private final PayoutService payoutService;

    // --- DOCTOR ENDPOINTS ---

    @PostMapping("/doctor/generate-bill/{scheduleId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<DoctorPayoutResultDTO>> generateDoctorBill(
            @PathVariable Integer scheduleId, Principal p) {

        DoctorPayoutResultDTO result = payoutService.generateDoctorBill(scheduleId, p.getName());
        return ResponseEntity.ok(ApiResponse.success(result, "Bill Generated"));
    }

    @GetMapping("/doctor/pending")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<PendingBillView>>> getPendingDoctorBills(
            @RequestParam(required = false) Integer doctorId
    ) {

        List<PendingBillView> list = payoutService.getPendingDoctorBills(doctorId);

        return ResponseEntity.ok(ApiResponse.success(
                list,
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

    // --- EXPENSE ENDPOINTS ---

    @PostMapping("/expense")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<OperationalExpense>> createExpense(
            @RequestBody ExpenseCreateDTO dto, Principal p) {

        OperationalExpense result = payoutService.addExpense(dto, p.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "Expense Added"));
    }
}