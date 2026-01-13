package com.dinethdf.ClinicManagementSystem.Service.Financial;

import com.dinethdf.ClinicManagementSystem.DTO.Financial.*;
import com.dinethdf.ClinicManagementSystem.Model.Financial.DoctorSchedulePayment;
import com.dinethdf.ClinicManagementSystem.Model.Financial.OperationalExpense;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface PayoutService {

    // --- DOCTOR PAYOUTS ---
    DoctorPayoutResultDTO generateDoctorBill(Integer scheduleId, String createdBy);

    FinancialSummaryDTO getFinancialSummary(LocalDate startDate, LocalDate endDate);

    // Update this line
    Page<PendingBillView> getPendingDoctorBills(Integer doctorId, int page, int size) ;

    Page<PayoutView> searchPayouts(Long doctorId, DoctorSchedulePayment.PayoutStatus status,
                                   LocalDate startDate, LocalDate endDate, int page, int size);

    BulkPaymentResponseDTO payDoctorsBulk(BulkPaymentRequestDTO request, String createdBy);

    @Transactional(rollbackFor = Exception.class)
    void reversePayment(Integer payoutId, String modifiedBy);

    // --- OPERATIONAL EXPENSES ---
    OperationalExpense addExpense(ExpenseCreateDTO dto, String createdBy);


    // --- LAB PAYOUTS (Placeholder for future logic) ---
    // void createLabBill(...);
}