package com.dinethdf.ClinicManagementSystem.Service.Payment;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.InvoicePaymentDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.InvoicePaymentDetailDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.PaymentResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.RefundRequestDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PaymentService {
    PaymentResponseDTO processInvoicePayment(InvoicePaymentDTO paymentDTO, Long userId);


    void processLateRefundOverride(com.dinethdf.ClinicManagementSystem.DTO.Payment.LateRefundOverrideDTO refundDTO);
    void processRefund(RefundRequestDTO refundDTO);
    List<InvoicePaymentDetailDTO> getPaymentHistory(Long invoiceId);
}