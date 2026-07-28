package com.dinethdf.ClinicManagementSystem.Service.Payment;

import com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff.ClinicStaffResponseDto;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.*;
import com.dinethdf.ClinicManagementSystem.Repo.Payment.PaymentRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Invoice.InvoiceRepo;
import com.dinethdf.ClinicManagementSystem.Model.Payment.Payment;
import com.dinethdf.ClinicManagementSystem.Model.Invoice.Invoice;
import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.DoctorSchedule;
import com.dinethdf.ClinicManagementSystem.Service.ClinicStaff.ClinicStaffService;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepo paymentRepo;
    private final InvoiceRepo invoiceRepo;
    private final ObjectMapper objectMapper;
    private final MessageSourceUtil messageSourceUtil;
    private final ClinicStaffService clinicStaffService;
//    private final SmsService smsService;

    @Override
    @Transactional
    public PaymentResponseDTO processInvoicePayment(InvoicePaymentDTO paymentDTO, Long userId) {

        ClinicStaffResponseDto staff = clinicStaffService.getStaffById(userId);

        validatePaymentDTO(paymentDTO);

        Integer clinicId = TenantContext.getCurrentTenant();

        String createdBy = staff.getFirstName() + " " + staff.getLastName();

        String receivedByName = staff.getFirstName() + " " + staff.getLastName();

        Integer receivedByStaffId = staff.getId().intValue();

        String paymentLocation = (paymentDTO.getPaymentLocation() != null)
                ? paymentDTO.getPaymentLocation()
                : "CLINIC_COUNTER";

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

        // Default to CASH if not provided
        String method = refundDTO.getRefundMethod() != null ? refundDTO.getRefundMethod().trim().toUpperCase() : "CASH";

        Integer clinicId = TenantContext.getCurrentTenant();
        String createdBy = "admin_user"; // Or fetch from Auth context

        // --- 2.5 PAYOUT LOCK GUARD ---
        Invoice invoice = invoiceRepo.findById(refundDTO.getInvoiceId())
                .orElseThrow(() -> new ValidationException("Invoice not found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        if (invoice.getAppointment() != null) {
            DoctorSchedule schedule = invoice.getAppointment().getDoctorSchedule();
            String payoutCode = schedule.getPayoutStatus() != null ? schedule.getPayoutStatus().getCode() : null;
            
            if ("PENDING".equals(payoutCode) || "PAID".equals(payoutCode)) {
                throw new ValidationException(
                        "Cannot process refund. The doctor's payout bill for this schedule has already been generated.",
                        "PAYOUT_LOCKED",
                        HttpStatus.CONFLICT
                );
            }
        }

        // 3. Call DB Procedure
        Map<String, Object> spResult = paymentRepo.sp_ProcessRefund(
                clinicId,
                refundDTO.getInvoiceId(),
                refundDTO.getAmount(),
                category,
                method, // NEW: Pass the method to the SP
                refundDTO.getReason(),
                refundDTO.getAuthorizedBy(),
                createdBy,
                false, // p_is_admin_override
                ""     // p_approval_note
        );

        // 4. Handle Result & Errors
        String statusMessage = (String) spResult.get("o_status_message");

        if (!"REFUND_SUCCESS".equals(statusMessage)) {
            // Map specific DB errors to user-friendly messages
            String displayMessage = "Failed to process refund.";
            if ("ERROR_INVOICE_NOT_FOUND".equals(statusMessage)) displayMessage = "Invoice not found.";
            if ("ERROR_REFUND_EXCEEDS_PAYMENT".equals(statusMessage)) displayMessage = "Refund amount cannot exceed the total amount paid.";
            if ("ERROR_INVALID_REFUND_CATEGORY".equals(statusMessage)) displayMessage = "Invalid refund category.";

            throw new ValidationException(displayMessage, "DB_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void processLateRefundOverride(LateRefundOverrideDTO refundDTO) {
        Integer clinicId = TenantContext.getCurrentTenant();
        
        // Use the system user/admin processing the override
        String createdBy = SecurityContextHolder.getContext().getAuthentication().getName();

        // Call DB Procedure
        Map<String, Object> spResult = paymentRepo.sp_ProcessRefund(
                clinicId,
                refundDTO.getInvoiceId(),
                refundDTO.getRefundAmount(),
                "CLINIC_LOSS", // Automatically overridden by SP anyway, but good to be explicit
                refundDTO.getRefundMethod(),
                "Late Refund Admin Override",
                createdBy, // authorized by self
                createdBy,
                true, // p_is_admin_override
                refundDTO.getApprovalNote() // p_approval_note
        );

        // Handle Result & Errors
        String statusMessage = (String) spResult.get("o_status_message");

        if (!"REFUND_SUCCESS".equals(statusMessage)) {
            String displayMessage = "Failed to process late refund override.";
            if ("ERROR_INVOICE_NOT_FOUND".equals(statusMessage)) displayMessage = "Invoice not found.";
            if ("ERROR_REFUND_EXCEEDS_PAYMENT".equals(statusMessage)) displayMessage = "Refund amount cannot exceed the total amount paid.";
            if ("ERROR_INVALID_REFUND_CATEGORY".equals(statusMessage)) displayMessage = "Invalid refund category.";
            if (statusMessage != null && statusMessage.startsWith("ERROR_DOCTOR_ALREADY_PAID")) {
                displayMessage = "Cannot process refund. The doctor has already been paid.";
            }

            throw new ValidationException(displayMessage, "DB_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<InvoicePaymentDetailDTO> getPaymentHistory(Long invoiceId) {

        Integer clinicId = TenantContext.getCurrentTenant();

        // 1. Fetch Data
        List<PaymentHistoryProjection> rawData = paymentRepo.findHistoryByInvoiceId(invoiceId, clinicId);

        // 2. Map to DTO
        return rawData.stream().map(row -> {
            InvoicePaymentDetailDTO dto = new InvoicePaymentDetailDTO();

            dto.setPaymentId(row.getPaymentId());
            dto.setPaymentDate(row.getPaymentDate());
            dto.setAmount(row.getAmount());
            dto.setTransactionRef(row.getTransactionRef());
            dto.setReceivedBy(row.getReceivedBy());
            dto.setMethod(row.getMethod());
            dto.setLocation(row.getLocation());

            // Map Status
            dto.setStatus(new InvoicePaymentDetailDTO.PaymentStatusDTO(
                    row.getStatusCode(),
                    row.getStatusDisplay(),
                    row.getStatusColor()
            ));

            return dto;
        }).collect(Collectors.toList());
    }

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