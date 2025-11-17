package com.dinethdf.ClinicManagementSystem.Repo.Payment;

import com.dinethdf.ClinicManagementSystem.Model.Payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Map;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {

    @Procedure(name = "Payment.sp_AppointmentPaymentProcess")
    Map<String, Object> sp_AppointmentPaymentProcess(
            @Param("p_invoice_id") Long invoiceId,
            @Param("p_payment_method") String paymentMethod,
            @Param("p_transaction_code") String transactionCode,
            @Param("p_created_by") String createdBy,
            @Param("p_discounts_json") String discountsJson
    );
}