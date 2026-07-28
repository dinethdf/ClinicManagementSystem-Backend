package com.dinethdf.ClinicManagementSystem.Controller.Payment;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.*;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.PatientPrincipal;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserPrincipal;
import com.dinethdf.ClinicManagementSystem.Service.Payment.PaymentService;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final MessageSourceUtil messageSourceUtil;

    @PostMapping("/appointment/pay")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> processPayment(
            @RequestBody InvoicePaymentDTO paymentDTO,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // 1. Get the ID from the authenticated token
        Long staffId = userPrincipal.getUserDetailsId();

        PaymentResponseDTO paymentResponse = paymentService.processInvoicePayment(paymentDTO, staffId);

        // 2. Wrap Response using your ApiResponse class
        ApiResponse<PaymentResponseDTO> response = ApiResponse.success(
                paymentResponse,
                messageSourceUtil.getMessage("payment.success") // Ensure this key exists in messages.properties
        );

        return ResponseEntity.ok(response);
    }


    @PostMapping("/appointment/refund")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR')") // Refunds are usually restricted
    public ResponseEntity<ApiResponse<Void>> processRefund(
            @RequestBody RefundRequestDTO refundDTO) {

        // 1. Call Service (Returns void if successful, throws exception if failed)
        paymentService.processRefund(refundDTO);

        // 2. Wrap Response (Data is null for void operations)
        ApiResponse<Void> response = ApiResponse.success(
                null,
                "Refund processed successfully" // You can also use messageSourceUtil here
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refund/admin-override")
    @PreAuthorize("hasAnyAuthority('ADMIN')") // Strict override access
    public ResponseEntity<ApiResponse<Void>> processLateRefundOverride(
            @RequestBody LateRefundOverrideDTO refundDTO) {

        paymentService.processLateRefundOverride(refundDTO);

        ApiResponse<Void> response = ApiResponse.success(
                null,
                "Late refund override processed successfully"
        );

        return ResponseEntity.ok(response);
    }

    // GET /api/payments/history/{invoiceId}
    @GetMapping("/history/{invoiceId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<InvoicePaymentDetailDTO>>> getPaymentHistory(
            @PathVariable Long invoiceId) {

        List<InvoicePaymentDetailDTO> history = paymentService.getPaymentHistory(invoiceId);

        return ResponseEntity.ok(ApiResponse.success(
                history,
                "Payment history retrieved successfully"
        ));
    }
}