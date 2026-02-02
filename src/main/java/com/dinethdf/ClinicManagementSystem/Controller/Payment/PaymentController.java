package com.dinethdf.ClinicManagementSystem.Controller.Payment;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.InvoicePaymentDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.InvoicePaymentDetailDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.PaymentResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.RefundRequestDTO;
import com.dinethdf.ClinicManagementSystem.Service.Payment.PaymentService;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
            @RequestBody InvoicePaymentDTO paymentDTO) {

        PaymentResponseDTO paymentResponse = paymentService.processInvoicePayment(paymentDTO);

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