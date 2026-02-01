package com.dinethdf.ClinicManagementSystem.Service.Payment;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.InvoicePaymentDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.PaymentResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.RefundRequestDTO;
import org.springframework.transaction.annotation.Transactional;

public interface PaymentService {
    PaymentResponseDTO processInvoicePayment(InvoicePaymentDTO paymentDTO);


    void processRefund(RefundRequestDTO refundDTO);
}