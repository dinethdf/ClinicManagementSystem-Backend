package com.dinethdf.ClinicManagementSystem.Repo.Payment;

import com.dinethdf.ClinicManagementSystem.DTO.Payment.PaymentHistoryProjection;
import com.dinethdf.ClinicManagementSystem.Model.Payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {

    @Procedure(procedureName = "sp_AppointmentPaymentProcess")
    Map<String, Object> sp_AppointmentPaymentProcess(
            @Param("p_clinic_id") Integer clinicId,
            @Param("p_invoice_id") Long invoiceId,
            @Param("p_amount_paid") BigDecimal amountPaid,       // NEW
            @Param("p_payment_method") String paymentMethod,
            @Param("p_transaction_code") String transactionCode,
            @Param("p_created_by") String createdBy,
            @Param("p_received_by") String receivedBy,           // NEW
            @Param("p_received_by_staff_id") Integer staffId,    // NEW
            @Param("p_payment_location") String location,        // NEW
            @Param("p_payment_gateway") String gateway,          // NEW
            @Param("p_discounts_json") String discountsJson
    );

    @Procedure(name = "Payment.sp_ProcessRefund")
    Map<String, Object> sp_ProcessRefund(
            @Param("p_clinic_id") Integer clinicId,
            @Param("p_invoice_id") Long invoiceId,
            @Param("p_refund_amount") BigDecimal refundAmount,
            @Param("p_refund_category") String refundCategory,
            @Param("p_reason") String reason,
            @Param("p_authorized_by") String authorizedBy,
            @Param("p_created_by") String createdBy
    );

    @Query(nativeQuery = true, value = """
        SELECT
            p.id AS paymentId,
            p.paymentTimestamp AS paymentDate,
            p.amountPaid AS amount,
            p.transactionId AS transactionRef,
            p.receivedBy AS receivedBy,
            pml.displayName AS method,
            pll.displayName AS location,
            psl.code AS statusCode,
            psl.displayName AS statusDisplay,
            psl.colorCode AS statusColor
        FROM payments p
        LEFT JOIN payment_method_lookup pml ON p.paymentMethodId = pml.id
        LEFT JOIN payment_location_lookup pll ON p.paymentLocationId = pll.id
        LEFT JOIN payment_status_lookup psl ON p.paymentStatusId = psl.id
        WHERE p.invoice_id = :invoiceId
          AND p.clinic_id = :clinicId
        ORDER BY p.paymentTimestamp DESC
    """)
    List<PaymentHistoryProjection> findHistoryByInvoiceId(
            @Param("invoiceId") Long invoiceId,
            @Param("clinicId") Integer clinicId
    );
}