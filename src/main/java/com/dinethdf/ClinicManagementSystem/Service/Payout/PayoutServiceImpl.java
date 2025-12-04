package com.dinethdf.ClinicManagementSystem.Service.Payout;

import com.dinethdf.ClinicManagementSystem.DTO.Financial.*;
import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.DoctorSchedule;
import com.dinethdf.ClinicManagementSystem.Model.Financial.*;
import com.dinethdf.ClinicManagementSystem.Repo.DoctorSchedule.DoctorScheduleRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Financial.FinancialRepo; // EXISTING REPO (For Calculations)
import com.dinethdf.ClinicManagementSystem.Repo.Payout.*;
import com.dinethdf.ClinicManagementSystem.Repo.Projection.ScheduleFinancialProjection;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PayoutServiceImpl implements PayoutService {

    private final DoctorSchedulePaymentRepo doctorBillRepo;
    private final OperationalExpenseRepo expenseRepo;
    private final ExpenseCategoryRepo categoryRepo;

    //    private final LabVendorPayoutRepo labRepo;

    private final DoctorScheduleRepo scheduleRepo;
    private final FinancialRepo calculationRepo;



    @Override
    public DoctorPayoutResultDTO generateDoctorBill(Integer scheduleId, String createdBy) {

        DoctorSchedule schedule = scheduleRepo.findById(Long.valueOf(scheduleId))
                .orElseThrow(() -> new ValidationException("Schedule not found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        if (doctorBillRepo.existsByDoctorScheduleId(scheduleId)) {
            throw new ValidationException("Bill already generated for this schedule", "DUPLICATE", HttpStatus.CONFLICT);
        }

        // CALL EXISTING STORED PROCEDURE
        List<ScheduleFinancialProjection> data = calculationRepo.getScheduleFinancials(scheduleId);

        if (data.isEmpty()) {
            throw new ValidationException("No appointment data found", "NO_DATA", HttpStatus.BAD_REQUEST);
        }

        // CALCULATE SUMS
        BigDecimal fee = data.stream().map(ScheduleFinancialProjection::getBaseDoctorFee).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = data.stream().map(ScheduleFinancialProjection::getDoctorDiscountAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal payable = data.stream().map(ScheduleFinancialProjection::getNetDoctorPayout).reduce(BigDecimal.ZERO, BigDecimal::add);

        // SAVE BILL
        DoctorSchedulePayment bill = new DoctorSchedulePayment();
        bill.setDoctorSchedule(schedule);
        bill.setTotalDoctorFee(fee);
        bill.setTotalDoctorDiscounts(discount);
        bill.setFinalPayableAmount(payable);
        bill.setStatus(DoctorSchedulePayment.PayoutStatus.PENDING);
        bill.setCreatedBy(createdBy);

        DoctorSchedulePayment saved = doctorBillRepo.save(bill);

        // MAP TO DTO
        DoctorPayoutResultDTO result = new DoctorPayoutResultDTO();
        result.setPayoutId(saved.getId());
        result.setScheduleId(scheduleId);
        result.setTotalFee(fee);
        result.setTotalDiscounts(discount);
        result.setFinalPayable(payable);
        result.setStatus(saved.getStatus());
        result.setMessage("Bill Generated. Ready for payment.");

        return result;
    }



    @Override
    public List<PendingBillView> getPendingDoctorBills(Integer doctorId) {
        return doctorBillRepo.findPendingBillsFiltered(DoctorSchedulePayment.PayoutStatus.PENDING, doctorId);
    }



    @Override
    public BulkPaymentResponseDTO payDoctorsBulk(BulkPaymentRequestDTO request, String createdBy) {
        List<Integer> ids = request.getPaymentIds();
        BigDecimal totalProcessed = BigDecimal.ZERO;
        int count = 0;

        if (ids == null || ids.isEmpty()) throw new ValidationException("No selection", "EMPTY", HttpStatus.BAD_REQUEST);

        for (Integer id : ids) {
            DoctorSchedulePayment bill = doctorBillRepo.findById(id)
                    .orElseThrow(() -> new ValidationException("Bill ID " + id + " not found", "NOT_FOUND", HttpStatus.NOT_FOUND));

            if (bill.getStatus() == DoctorSchedulePayment.PayoutStatus.PAID) {
                throw new ValidationException("Schedule #" + bill.getDoctorSchedule().getId() + " is already paid", "PAID", HttpStatus.CONFLICT);
            }

            BigDecimal amount = bill.getFinalPayableAmount();

            // CREATE TRANSACTION RECORD
            PayoutTransaction tx = new PayoutTransaction();
            tx.setDoctorPayment(bill);
            tx.setAmountPaid(amount);
            tx.setPaymentMethod(request.getPaymentMethod());
            tx.setTransactionReference(request.getTransactionReference());
            tx.setCreatedBy(createdBy);

            // UPDATE BILL STATUS
            bill.setStatus(DoctorSchedulePayment.PayoutStatus.PAID);

            if (bill.getTransactions() == null) bill.setTransactions(new ArrayList<>());
            bill.getTransactions().add(tx);

            doctorBillRepo.save(bill);

            totalProcessed = totalProcessed.add(amount);
            count++;
        }

        // SAFETY CHECK
        if (request.getTotalAmountToPay() != null && totalProcessed.compareTo(request.getTotalAmountToPay()) != 0) {
            throw new ValidationException("Total mismatch. Calculated: " + totalProcessed, "MISMATCH", HttpStatus.BAD_REQUEST);
        }

        BulkPaymentResponseDTO response = new BulkPaymentResponseDTO();
        response.setSuccessCount(count);
        response.setTotalProcessedAmount(totalProcessed);
        response.setMessage("Paid " + count + " doctors Schedules successfully.");
        return response;
    }


    @Override
    public OperationalExpense addExpense(ExpenseCreateDTO dto, String createdBy) {

        ExpenseCategory cat = categoryRepo.findById(dto.getExpenseCategoryId())
                .orElseThrow(() -> new ValidationException("Invalid Category ID", "INVALID_CAT", HttpStatus.BAD_REQUEST));

        OperationalExpense exp = new OperationalExpense();
        exp.setExpenseCategory(cat);
        exp.setDescription(dto.getDescription());
        exp.setAmount(dto.getAmount());
        exp.setDueDate(dto.getDueDate());
        exp.setStatus(DoctorSchedulePayment.PayoutStatus.PENDING);
        exp.setCreatedBy(createdBy);

        return expenseRepo.save(exp);
    }
}