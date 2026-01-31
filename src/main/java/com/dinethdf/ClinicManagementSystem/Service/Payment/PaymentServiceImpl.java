package com.dinethdf.ClinicManagementSystem.Service.Payment;

import com.dinethdf.ClinicManagementSystem.DTO.Payment.InvoicePaymentDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.PaymentResponseDTO;
import com.dinethdf.ClinicManagementSystem.Repo.Payment.PaymentRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Invoice.InvoiceRepo;
import com.dinethdf.ClinicManagementSystem.Model.Payment.Payment;
import com.dinethdf.ClinicManagementSystem.config.TenantContext; // <--- Import Context
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepo paymentRepo;
    private final InvoiceRepo invoiceRepo;
    private final ObjectMapper objectMapper;
    private final MessageSourceUtil messageSourceUtil;
//    private final SmsService smsService;

    @Override
    @Transactional
    public PaymentResponseDTO processInvoicePayment(InvoicePaymentDTO paymentDTO) {

        validatePaymentDTO(paymentDTO);

        // 1. Context Data
        Integer clinicId = TenantContext.getCurrentTenant();
        String createdBy = "receptionist_user"; // Replace with SecurityContext username

        // 2. Prepare Data (Defaults)
        String receivedByName = "Unknown Cashier"; // Ideally fetch from logged-in user
        Integer receivedByStaffId = 1;             // Ideally fetch from logged-in user ID
        String paymentLocation = "CLINIC_COUNTER"; // Default for reception payments

        // 3. Prepare JSON
        String discountsJson = null;
        if (paymentDTO.getDiscounts() != null && !paymentDTO.getDiscounts().isEmpty()) {
            try {
                discountsJson = objectMapper.writeValueAsString(paymentDTO.getDiscounts());
            } catch (JsonProcessingException e) {
                throw new ValidationException("Invalid discount format", "JSON_ERROR", HttpStatus.BAD_REQUEST);
            }
        }

        // 4. Call the Stored Procedure (Updated Signature)
        Map<String, Object> spResult = paymentRepo.sp_AppointmentPaymentProcess(
                clinicId,
                paymentDTO.getInvoiceId(),
                paymentDTO.getAmountPaid(), // Ensure DTO has this!
                "CASH",
                paymentDTO.getTransactionCode(),
                createdBy,
                receivedByName,      // p_received_by
                receivedByStaffId,   // p_received_by_staff_id
                paymentLocation,     // p_payment_location
                null,                // p_payment_gateway (Null for physical)
                discountsJson
        );

// --- DEBUGGING START ---
        System.out.println("FULL DB RESULT: " + spResult); // Check your console logs for this!
        // --- DEBUGGING END ---

        // 5. Check for SP-level errors
        String statusMessage = (String) spResult.get("o_status_message");

        // FIX: Check if message starts with PAYMENT_SUCCESS (covers FULL and PARTIAL)
        if (statusMessage == null || !statusMessage.startsWith("PAYMENT_SUCCESS")) {
            // FIX: Throw the ACTUAL message from the DB, not a generic one
            throw new ValidationException(
                    "DB Error: " + statusMessage,
                    "DATABASE_ERROR",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 6. Retrieve the new Payment Object
        Integer newPaymentId = (Integer) spResult.get("o_new_payment_id");
        BigDecimal remainingBalance = (BigDecimal) spResult.get("o_remaining_balance");

        Payment newPayment = paymentRepo.findById(newPaymentId.longValue())
                .orElseThrow(() -> new ValidationException("Failed to retrieve new payment", "PAYMENT_NOT_FOUND", HttpStatus.INTERNAL_SERVER_ERROR));

        // 7. Security Check
        if (!Objects.equals(newPayment.getClinicId(), clinicId)) {
            throw new ValidationException("Access Denied", "UNAUTHORIZED", HttpStatus.FORBIDDEN);
        }

        // 8. SMS Logic (Simplified)
        String smsDeliveryNote = "SMS_SENT_SUCCESSFULLY";
        try {
            // Your SMS logic here...
            if (true) { // Simulator
                smsDeliveryNote = "PAYMENT_SUCCESS_BUT_SMS_FAILED";
            }
        } catch (Exception e) {
            smsDeliveryNote = "SMS_ERROR: " + e.getMessage();
        }

        // 9. Prepare Response (Updated Mapping)
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setId(newPayment.getId());
        response.setInvoiceId(newPayment.getInvoice().getId());
        response.setAmountPaid(newPayment.getAmountPaid());
        response.setRemainingBalance(remainingBalance);

        if (newPayment.getPaymentMethod() != null) {
            response.setPaymentMethod(newPayment.getPaymentMethod().getDisplayName());
        }

        response.setTransactionId(newPayment.getTransactionId());

        // Map Rich Status Object
        if (newPayment.getPaymentStatus() != null) {
            PaymentResponseDTO.PaymentStatusDTO statusDTO = new PaymentResponseDTO.PaymentStatusDTO();
            statusDTO.setCode(newPayment.getPaymentStatus().getCode());
            statusDTO.setDisplayName(newPayment.getPaymentStatus().getDisplayName());
            statusDTO.setColorCode(newPayment.getPaymentStatus().getColorCode());
            response.setPaymentStatus(statusDTO);
        }

        response.setCreatedDate(newPayment.getCreatedDate());
        response.setSmsStatus(smsDeliveryNote);

        return response;
    }

    // --- Helper Method for Phone Number ---
    private String formatToLankaInternational(String mobile) {
        if (mobile == null) return "";
        mobile = mobile.trim();

        // If starts with 0 (e.g., 077...), remove 0 and add 94
        if (mobile.startsWith("0")) {
            return "94" + mobile.substring(1);
        }
        // If already starts with 94, return as is
        if (mobile.startsWith("94")) {
            return mobile;
        }
        // Fallback (or throw validation error if strict)
        return "94" + mobile;
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
                    messageSourceUtil.getMessage("appointment.already.paid"),
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