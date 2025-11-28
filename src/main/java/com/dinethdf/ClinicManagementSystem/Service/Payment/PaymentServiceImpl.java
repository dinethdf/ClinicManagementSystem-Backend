package com.dinethdf.ClinicManagementSystem.Service.Payment;

import com.dinethdf.ClinicManagementSystem.DTO.Payment.InvoicePaymentDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.PaymentResponseDTO;
import com.dinethdf.ClinicManagementSystem.Repo.Payment.PaymentRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Invoice.InvoiceRepo;
import com.dinethdf.ClinicManagementSystem.Model.Payment.Payment;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import com.dinethdf.ClinicManagementSystem.util.SmsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepo paymentRepo;
    private final InvoiceRepo invoiceRepo;
    private final ObjectMapper objectMapper;
    private final MessageSourceUtil messageSourceUtil;
    private final SmsService smsService;

    @Override
    @Transactional // Ensure DB transaction is separate from SMS logic
    public PaymentResponseDTO processInvoicePayment(InvoicePaymentDTO paymentDTO) {

        validatePaymentDTO(paymentDTO);


        String createdBy = "receptionist_user";

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

        // 6. Retrieve the new Payment Object (Success Case)
        Integer newPaymentId = (Integer) spResult.get("o_new_payment_id");
        Payment newPayment = paymentRepo.findById(newPaymentId.longValue())
                .orElseThrow(() -> new ValidationException("Failed to retrieve new payment", "PAYMENT_NOT_FOUND", HttpStatus.INTERNAL_SERVER_ERROR));


        String smsDeliveryNote = "SMS_SENT_SUCCESSFULLY";

        try {
            String rawMobile = paymentDTO.getPatientMobileNo();
            String formattedMobile = formatToLankaInternational(rawMobile);

            String msgText = "Payment Received. Ref: " + newPayment.getTransactionId() + ". Amount: " + newPayment.getAmountPaid() + "Appointment Booked";


            boolean isSent = smsService.sendPaymentSms(formattedMobile, msgText);

            if (!isSent) {
                smsDeliveryNote = "PAYMENT_SUCCESS_BUT_SMS_FAILED";
            }

        } catch (Exception e) {
            // CATCH ALL: Ensure Payment success is returned even if SMS crashes
            System.err.println("SMS Sending Error: " + e.getMessage());
            smsDeliveryNote = "PAYMENT_SUCCESS_BUT_SMS_ERROR: " + e.getMessage();
        }


        // 8. Prepare Response

        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setId(newPayment.getId());
        response.setInvoiceId(newPayment.getInvoice().getId());
        response.setAmountPaid(newPayment.getAmountPaid());
        response.setPaymentMethod(newPayment.getPaymentMethod().toString());
        response.setTransactionId(newPayment.getTransactionId());
        response.setPaymentStatus(newPayment.getPaymentStatus().toString());
        response.setCreatedDate(newPayment.getCreatedDate());

        // Set the SMS status here so Frontend knows to show a warning if needed
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