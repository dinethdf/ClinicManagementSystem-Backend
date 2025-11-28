package com.dinethdf.ClinicManagementSystem.Service.Payment;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.InvoicePaymentDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Payment.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO processInvoicePayment(InvoicePaymentDTO paymentDTO);
}