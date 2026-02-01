package com.dinethdf.ClinicManagementSystem.Service.Payment;

import com.dinethdf.ClinicManagementSystem.DTO.Payment.InvoicePaymentDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.PaymentResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.RefundRequestDTO; // <--- Ensure you create this DTO
import com.dinethdf.ClinicManagementSystem.Repo.Payment.PaymentRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Invoice.InvoiceRepo;
import com.dinethdf.ClinicManagementSystem.Model.Payment.Payment;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
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
        String createdBy = "receptionist_user"; // TODO: Fetch from SecurityContext

        // 2. Prepare Data (Defaults)
        String receivedByName = "Unknown Cashier"; // TODO: Fetch from Logged-in User
        Integer receivedByStaffId = 1;             // TODO: Fetch from Logged-in User ID

        // Use DTO location if provided, else default
        String paymentLocation = (paymentDTO.getPaymentLocation() != null)
                ? paymentDTO.getPaymentLocation()
                : "CLINIC_COUNTER";

        // 3. Prepare JSON for Discounts
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
                paymentDTO.getPaymentMethod(),
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
            if (true) {
                smsDeliveryNote = "PAYMENT_SUCCESS_BUT_SMS_FAILED";
            }
        } catch (Exception e) {
            smsDeliveryNote = "SMS_ERROR: " + e.getMessage();
        }

        // 9. Prepare Response
        return mapToPaymentResponse(newPayment, remainingBalance, smsDeliveryNote);
    }

    @Override
    @Transactional
    public void processRefund(RefundRequestDTO refundDTO) {

        // --- 1. MANUAL VALIDATION ---
        validateRefundRequest(refundDTO);

        // --- 2. LOGIC ---
        String category = refundDTO.getRefundCategory().trim().toUpperCase();

        Integer clinicId = TenantContext.getCurrentTenant();
        String createdBy = "admin_user";

        Map<String, Object> spResult = paymentRepo.sp_ProcessRefund(
                clinicId,
                refundDTO.getInvoiceId(),
                refundDTO.getAmount(),
                category,
                refundDTO.getReason(),
                refundDTO.getAuthorizedBy(),
                createdBy
        );

        // 4. Handle Result
        String statusMessage = (String) spResult.get("o_status_message");

        if (!"REFUND_SUCCESS".equals(statusMessage)) {
            throw new ValidationException(
                    "Refund Failed: " + statusMessage,
                    "REFUND_ERROR",
                    HttpStatus.BAD_REQUEST
            );
        }

        // ... existing error handling ...
    }
    // =================================================================================
    // 3. HELPER METHODS
    // =================================================================================
    private void validateRefundRequest(RefundRequestDTO dto) {
        if (dto.getInvoiceId() == null) {
            throw new ValidationException("Invoice ID is required", "INVALID_INPUT", HttpStatus.BAD_REQUEST);
        }

        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Refund amount must be greater than 0", "INVALID_AMOUNT", HttpStatus.BAD_REQUEST);
        }

        if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
            throw new ValidationException("Reason is required", "INVALID_INPUT", HttpStatus.BAD_REQUEST);
        }

        if (dto.getAuthorizedBy() == null || dto.getAuthorizedBy().trim().isEmpty()) {
            throw new ValidationException("Authorized By is required", "INVALID_INPUT", HttpStatus.BAD_REQUEST);
        }

        if (dto.getRefundCategory() == null) {
            throw new ValidationException("Refund Category is required", "INVALID_INPUT", HttpStatus.BAD_REQUEST);
        }

        String cat = dto.getRefundCategory().trim().toUpperCase();
        if (!cat.equals("DOCTOR") && !cat.equals("CLINIC")) {
            throw new ValidationException("Category must be 'DOCTOR' or 'CLINIC'", "INVALID_CATEGORY", HttpStatus.BAD_REQUEST);
        }
    }


    private PaymentResponseDTO mapToPaymentResponse(Payment payment, BigDecimal remainingBalance, String smsStatus) {
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setId(payment.getId());
        response.setInvoiceId(payment.getInvoice().getId());
        response.setAmountPaid(payment.getAmountPaid());
        response.setRemainingBalance(remainingBalance);
        response.setTransactionId(payment.getTransactionId());
        response.setCreatedDate(payment.getCreatedDate());
        response.setSmsStatus(smsStatus);

        if (payment.getPaymentMethod() != null) {
            response.setPaymentMethod(payment.getPaymentMethod().getDisplayName());
        }

        if (payment.getPaymentStatus() != null) {
            PaymentResponseDTO.PaymentStatusDTO statusDTO = new PaymentResponseDTO.PaymentStatusDTO();
            statusDTO.setCode(payment.getPaymentStatus().getCode());
            statusDTO.setDisplayName(payment.getPaymentStatus().getDisplayName());
            statusDTO.setColorCode(payment.getPaymentStatus().getColorCode());
            response.setPaymentStatus(statusDTO);
        }

        return response;
    }

    private String formatToLankaInternational(String mobile) {
        if (mobile == null) return "";
        mobile = mobile.trim();
        if (mobile.startsWith("0")) return "94" + mobile.substring(1);
        if (mobile.startsWith("94")) return mobile;
        return "94" + mobile;
    }

    private void validatePaymentDTO(InvoicePaymentDTO dto) {
        if (dto.getInvoiceId() == null) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("payment.error.no_invoice_id"),
                    "INVALID_INVOICE_ID", HttpStatus.BAD_REQUEST
            );
        }
        if (dto.getPaymentMethod() == null || dto.getPaymentMethod().isEmpty()) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("payment.error.no_method"),
                    "INVALID_PAYMENT_METHOD", HttpStatus.BAD_REQUEST
            );
        }
        // Validate Payment Location (New Requirement)
        if (dto.getPaymentLocation() == null || dto.getPaymentLocation().isEmpty()) {
            // Optional: Set default here if you don't want to throw error, or enforce it.
            // For now, we set default in the main method, so this check is skipped.
        }
    }
}