package com.dinethdf.ClinicManagementSystem.Service.Payout;

import com.dinethdf.ClinicManagementSystem.DTO.Financial.*;
import com.dinethdf.ClinicManagementSystem.Model.Financial.DoctorSchedulePayment;
import com.dinethdf.ClinicManagementSystem.Model.Financial.OperationalExpense;
import java.util.List;

public interface PayoutService {

    // --- DOCTOR PAYOUTS ---
    DoctorPayoutResultDTO generateDoctorBill(Integer scheduleId, String createdBy);
    List<DoctorSchedulePayment> getPendingDoctorBills();
    BulkPaymentResponseDTO payDoctorsBulk(BulkPaymentRequestDTO request, String createdBy);

    // --- OPERATIONAL EXPENSES ---
    OperationalExpense addExpense(ExpenseCreateDTO dto, String createdBy);


    // --- LAB PAYOUTS (Placeholder for future logic) ---
    // void createLabBill(...);
}