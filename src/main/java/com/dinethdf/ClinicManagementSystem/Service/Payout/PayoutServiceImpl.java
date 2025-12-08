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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public FinancialSummaryDTO getFinancialSummary(LocalDate startDate, LocalDate endDate) {

        BigDecimal totalPaidToDoctors = doctorBillRepo.sumByStatusAndDate(
                DoctorSchedulePayment.PayoutStatus.PAID, startDate, endDate
        );

        BigDecimal totalPendingDoctors = doctorBillRepo.sumByStatusAndDate(
                DoctorSchedulePayment.PayoutStatus.PENDING, startDate, endDate
        );

        BigDecimal totalExpenses = expenseRepo.sumExpensesByDate(startDate, endDate);

        return new FinancialSummaryDTO(totalPaidToDoctors, totalPendingDoctors, totalExpenses);
    }


    @Override
    public List<PendingBillView> getPendingDoctorBills(Integer doctorId) {
        return doctorBillRepo.findPendingBillsFiltered(DoctorSchedulePayment.PayoutStatus.PENDING, doctorId);
    }

    @Override
    public Page<PayoutView> searchPayouts(Long doctorId, DoctorSchedulePayment.PayoutStatus status,
                                          LocalDate startDate, LocalDate endDate, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("ds.scheduleDate").descending());
        return doctorBillRepo.searchPayouts(doctorId, status, startDate, endDate, pageable);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BulkPaymentResponseDTO payDoctorsBulk(BulkPaymentRequestDTO request, String createdBy) {

        List<Integer> ids = request.getPaymentIds();

        // --- 1. BASIC VALIDATION ---
        if (ids == null || ids.isEmpty()) {
            throw new ValidationException("No selection", "EMPTY", HttpStatus.BAD_REQUEST);
        }

        // --- 2. FETCH ALL DATA (One DB Query) ---
        // Efficiently fetch all bills in one go
        List<DoctorSchedulePayment> bills = doctorBillRepo.findAllById(ids);

        // Check if all requested IDs were found
        if (bills.size() != ids.size()) {
            throw new ValidationException("One or more Bill IDs not found in database", "NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        // --- 3. LOGIC VALIDATION LOOP (Memory Only - No Saves Yet) ---
        Long referenceDoctorId = null;
        BigDecimal calculatedTotal = BigDecimal.ZERO;

        for (DoctorSchedulePayment bill : bills) {

            // A. Check Status
            if (bill.getStatus() == DoctorSchedulePayment.PayoutStatus.PAID) {
                throw new ValidationException(
                        "Schedule #" + bill.getDoctorSchedule().getId() + " is already paid. Transaction Aborted.",
                        "ALREADY_PAID",
                        HttpStatus.CONFLICT
                );
            }

            // B. Check Doctor Consistency (Must be same doctor)
            Long currentDoctorId = bill.getDoctorSchedule().getDoctor().getId();

            if (referenceDoctorId == null) {
                referenceDoctorId = currentDoctorId; // Set the first doctor as the standard
            } else {
                if (!currentDoctorId.equals(referenceDoctorId)) {
                    throw new ValidationException(
                            "Multi-Doctor Payment Not Allowed. You selected schedules for different doctors (IDs: "
                                    + referenceDoctorId + " and " + currentDoctorId + "). Please select one doctor at a time.",
                            "MIXED_DOCTORS",
                            HttpStatus.BAD_REQUEST
                    );
                }
            }

            // C. Sum up total
            calculatedTotal = calculatedTotal.add(bill.getFinalPayableAmount());
        }

        if (request.getTotalAmountToPay() != null && calculatedTotal.compareTo(request.getTotalAmountToPay()) != 0) {
            throw new ValidationException(
                    "Total mismatch! System calculated: " + calculatedTotal + ", but you requested: " + request.getTotalAmountToPay(),
                    "AMOUNT_MISMATCH",
                    HttpStatus.BAD_REQUEST
            );
        }

        int count = 0;
        List<DoctorSchedulePayment> billsToSave = new ArrayList<>();

        for (DoctorSchedulePayment bill : bills) {

            // 1. Create Transaction Record
            PayoutTransaction tx = new PayoutTransaction();
            tx.setDoctorPayment(bill);
            tx.setAmountPaid(bill.getFinalPayableAmount());
            tx.setPaymentMethod(request.getPaymentMethod());
            tx.setTransactionReference(request.getTransactionReference());
            tx.setCreatedBy(createdBy);

            // 2. Update Bill Status
            bill.setStatus(DoctorSchedulePayment.PayoutStatus.PAID);

            // 3. Link Transaction
            if (bill.getTransactions() == null) bill.setTransactions(new ArrayList<>());
            bill.getTransactions().add(tx);

            // Add to batch list
            billsToSave.add(bill);
            count++;
        }

        // 4. Batch Save (Efficient)
        doctorBillRepo.saveAll(billsToSave);

        // --- RETURN SUCCESS ---
        BulkPaymentResponseDTO response = new BulkPaymentResponseDTO();
        response.setSuccessCount(count);
        response.setTotalProcessedAmount(calculatedTotal);
        response.setMessage("Validation passed. Successfully paid " + count + " schedules.");

        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void reversePayment(Integer payoutId, String modifiedBy) {

        // 1. Get the Bill
        DoctorSchedulePayment bill = doctorBillRepo.findById(payoutId)
                .orElseThrow(() -> new ValidationException("Bill not found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        // 2. Validate: Can only reverse PAID items
        if (bill.getStatus() != DoctorSchedulePayment.PayoutStatus.PAID) {
            throw new ValidationException("Only PAID bills can be reversed", "INVALID_STATUS", HttpStatus.BAD_REQUEST);
        }

        // 3. Reset Status
        bill.setStatus(DoctorSchedulePayment.PayoutStatus.PENDING);
        bill.setUpdatedBy(modifiedBy);
        bill.setUpdatedDate(LocalDateTime.now());

        bill.getTransactions().clear();

        doctorBillRepo.save(bill);
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