package com.dinethdf.ClinicManagementSystem.Controller.Payment;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.InvoicePaymentDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.PaymentResponseDTO;
import com.dinethdf.ClinicManagementSystem.Service.Payment.PaymentService;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final MessageSourceUtil messageSourceUtil;

    @PostMapping("/appointment/pay")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> processPayment(
            @RequestBody InvoicePaymentDTO paymentDTO) {

        PaymentResponseDTO paymentResponse = paymentService.processInvoicePayment(paymentDTO);

        ApiResponse<PaymentResponseDTO> response = ApiResponse.success(
                paymentResponse,
                messageSourceUtil.getMessage("payment.success")
        );

        return ResponseEntity.ok(response);
    }
}