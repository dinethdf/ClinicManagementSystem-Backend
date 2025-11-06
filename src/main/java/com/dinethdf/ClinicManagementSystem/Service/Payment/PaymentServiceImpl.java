package com.dinethdf.ClinicManagementSystem.Service.Payment;

import com.dinethdf.ClinicManagementSystem.DTO.Payment.InvoicePaymentDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.PaymentResponseDTO;
import com.dinethdf.ClinicManagementSystem.Repo.Payment.PaymentRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Invoice.InvoiceRepo;
import com.dinethdf.ClinicManagementSystem.Model.Payment.Payment;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepo paymentRepo;
    private final InvoiceRepo invoiceRepo;
    private final ObjectMapper objectMapper;
    private final MessageSourceUtil messageSourceUtil;

    @Override
    public PaymentResponseDTO processInvoicePayment(InvoicePaymentDTO paymentDTO) {

        // --- 1. NEW MANUAL VALIDATION ---
        validatePaymentDTO(paymentDTO);

        // --- 2. Get logged-in user ---
        String createdBy = "receptionist_user"; // TODO: Get from Spring Security

        // 3. Serialize discounts
        String discountsJson = null;
        if (paymentDTO.getDiscounts() != null && !paymentDTO.getDiscounts().isEmpty()) {
            try {
                discountsJson = objectMapper.writeValueAsString(paymentDTO.getDiscounts());
            } catch (JsonProcessingException e) {
                throw new ValidationException("Invalid discount format", "JSON_ERROR", HttpStatus.BAD_REQUEST);
            }
        }

        // 4. Call the Stored Procedure
        Map<String, Object> spResult = paymentRepo.sp_AppointmentPaymentProcess(
                paymentDTO.getInvoiceId(),
                paymentDTO.getPaymentMethod(),
                paymentDTO.getTransactionCode(),
                createdBy,
                discountsJson
        );

        // 5. Check for SP-level errors
        String statusMessage = (String) spResult.get("o_status_message");
        if (!"PAYMENT_SUCCESS".equals(statusMessage)) {
            handleSpError(statusMessage);
        }

        // 6. Success
        Integer newPaymentId = (Integer) spResult.get("o_new_payment_id");
        Payment newPayment = paymentRepo.findById(newPaymentId.longValue())
                .orElseThrow(() -> new ValidationException("Failed to retrieve new payment", "PAYMENT_NOT_FOUND", HttpStatus.INTERNAL_SERVER_ERROR));

        // ... mapping logic ...
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setId(newPayment.getId());
        response.setInvoiceId(newPayment.getInvoice().getId());
        response.setAmountPaid(newPayment.getAmountPaid());
        response.setPaymentMethod(newPayment.getPaymentMethod());
        response.setTransactionId(newPayment.getTransactionId());
        response.setPaymentStatus(newPayment.getPaymentStatus());
        response.setCreatedDate(newPayment.getCreatedDate());

        return response;
    }

    /**
     * --- NEW HELPER METHOD ---
     * Manually validates the DTO.
     */
    private void validatePaymentDTO(InvoicePaymentDTO dto) {
        if (dto.getInvoiceId() == null) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("payment.error.no_invoice_id"),
                    "INVALID_INVOICE_ID",
                    HttpStatus.BAD_REQUEST
            );
        }
        if (dto.getPaymentMethod() == null || dto.getPaymentMethod().isEmpty()) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("payment.error.no_method"),
                    "INVALID_PAYMENT_METHOD",
                    HttpStatus.BAD_REQUEST
            );
        }
        if ("Card".equals(dto.getPaymentMethod()) && (dto.getTransactionCode() == null || dto.getTransactionCode().isEmpty())) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("payment.error.no_card_code"),
                    "INVALID_TRANSACTION_CODE",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void handleSpError(String statusMessage) {
        if ("ERROR_INVOICE_PAID".equals(statusMessage)) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("payment.error.paid"),
                    "INVOICE_PAID",
                    HttpStatus.CONFLICT
            );
        }
        throw new ValidationException(
                messageSourceUtil.getMessage("payment.error.database"),
                "DATABASE_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}