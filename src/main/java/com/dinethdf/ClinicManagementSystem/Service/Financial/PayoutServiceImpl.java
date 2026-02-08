package com.dinethdf.ClinicManagementSystem.Service.Financial;

import com.dinethdf.ClinicManagementSystem.DTO.Financial.*;
import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.DoctorSchedule;
import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.PayoutStatusLookup; // <--- Import Lookup
import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.ScheduleStatusLookup; // <--- Import Lookup
import com.dinethdf.ClinicManagementSystem.Model.Financial.*;
import com.dinethdf.ClinicManagementSystem.Repo.DoctorSchedule.DoctorScheduleRepo;
import com.dinethdf.ClinicManagementSystem.Repo.DoctorSchedule.PayoutStatusLookupRepo; // <--- Import Repo
import com.dinethdf.ClinicManagementSystem.Repo.DoctorSchedule.ScheduleStatusLookupRepo; // <--- Import Repo
import com.dinethdf.ClinicManagementSystem.Repo.Financial.DoctorSchedulePaymentRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Financial.ExpenseCategoryRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Financial.FinancialRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Financial.OperationalExpenseRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Projection.ScheduleFinancialProjection;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
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
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class PayoutServiceImpl implements PayoutService {

    private final DoctorSchedulePaymentRepo doctorBillRepo;
    private final OperationalExpenseRepo expenseRepo;
    private final ExpenseCategoryRepo categoryRepo;
    private final DoctorScheduleRepo scheduleRepo;
    private final FinancialRepo calculationRepo;

    // --- NEW: Inject Lookup Repos ---
    private final ScheduleStatusLookupRepo scheduleStatusRepo;
    private final PayoutStatusLookupRepo payoutStatusRepo;

    @Override
    public DoctorPayoutResultDTO generateDoctorBill(Long scheduleId, String createdBy) {

        Integer clinicId = TenantContext.getCurrentTenant();

        // 1. Fetch Schedule & Validate Tenant Ownership
        DoctorSchedule schedule = scheduleRepo.findById(Long.valueOf(scheduleId))
                .orElseThrow(() -> new ValidationException("Schedule not found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!Objects.equals(schedule.getClinicId(), clinicId)) {
            throw new ValidationException("Schedule not found", "NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        // 2. Validate Duplicate (Scoped to Clinic)
        if (doctorBillRepo.existsByDoctorScheduleIdAndClinicId(scheduleId, clinicId)) {
            throw new ValidationException("Bill already generated for this schedule", "DUPLICATE", HttpStatus.CONFLICT);
        }

        // 3. Get Financials (Scoped to Clinic)
        List<ScheduleFinancialProjection> data = calculationRepo.getScheduleFinancials(scheduleId, clinicId);

        if (data.isEmpty()) {
            throw new ValidationException("No appointment data found. Cannot generate bill.", "NO_DATA", HttpStatus.BAD_REQUEST);
        }

        // 4. Sum up totals
        BigDecimal fee = data.stream().map(ScheduleFinancialProjection::getBaseDoctorFee).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = data.stream().map(ScheduleFinancialProjection::getDoctorDiscountAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal payable = data.stream().map(ScheduleFinancialProjection::getNetDoctorPayout).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Create the Bill (Liability)
        DoctorSchedulePayment bill = new DoctorSchedulePayment();
        bill.setClinicId(clinicId);
        bill.setDoctorSchedule(schedule);
        bill.setTotalDoctorFee(fee);
        bill.setTotalDoctorDiscounts(discount);
        bill.setFinalPayableAmount(payable);
        bill.setStatus(DoctorSchedulePayment.PayoutStatus.PENDING); // This Enum likely belongs to the Payment Model, not Schedule Model, so it might be fine.
        bill.setCreatedBy(createdBy);

        DoctorSchedulePayment saved = doctorBillRepo.save(bill);

        // 6. Update Schedule Status (UPDATED)
        // Fetch 'PENDING' Payout Status
        PayoutStatusLookup pendingPayout = payoutStatusRepo.findByCode("PENDING")
                .orElseThrow(() -> new ValidationException("System Error: Payout Status 'PENDING' not found", "CONFIG_ERROR"));

        // Fetch 'COMPLETED' Schedule Status
        ScheduleStatusLookup completedSchedule = scheduleStatusRepo.findByCode("COMPLETED")
                .orElseThrow(() -> new ValidationException("System Error: Schedule Status 'COMPLETED' not found", "CONFIG_ERROR"));

        schedule.setPayoutStatus(pendingPayout);
        schedule.setStatus(completedSchedule);
        scheduleRepo.save(schedule);

        // 7. Return Result
        DoctorPayoutResultDTO result = new DoctorPayoutResultDTO();
        result.setPayoutId(saved.getId());
        result.setScheduleId(scheduleId);
        result.setTotalFee(fee);
        result.setTotalDiscounts(discount);
        result.setFinalPayable(payable);
        result.setStatus(saved.getStatus());
        result.setMessage("Bill Generated. Schedule marked as PENDING.");

        return result;
    }

    @Override
    public FinancialSummaryDTO getFinancialSummary(LocalDate startDate, LocalDate endDate) {

        Integer clinicId = TenantContext.getCurrentTenant();

        // Pass Clinic ID to all sum queries
        BigDecimal totalPaidToDoctors = doctorBillRepo.sumByStatusAndDate(
                DoctorSchedulePayment.PayoutStatus.PAID, startDate, endDate, clinicId
        );

        BigDecimal totalPendingDoctors = doctorBillRepo.sumByStatusAndDate(
                DoctorSchedulePayment.PayoutStatus.PENDING, startDate, endDate, clinicId
        );

        BigDecimal totalExpenses = expenseRepo.sumExpensesByDate(startDate, endDate, clinicId);

        return new FinancialSummaryDTO(totalPaidToDoctors, totalPendingDoctors, totalExpenses);
    }

    @Override
    public Page<PendingBillView> getPendingDoctorBills(Long doctorId, int page, int size) {
        // 1. Create PageRequest (Sort by date usually makes sense for bills)
        Pageable pageable = PageRequest.of(page, size, Sort.by("ds.scheduleDate").descending());

        // 2. Fetch only the requested slice
        return doctorBillRepo.findPendingBillsFiltered(
                DoctorSchedulePayment.PayoutStatus.PENDING,
                doctorId,
                TenantContext.getCurrentTenant(),
                pageable
        );
    }

    @Override
    public Page<PayoutView> searchPayouts(Long doctorId, DoctorSchedulePayment.PayoutStatus status,
                                          LocalDate startDate, LocalDate endDate, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("ds.scheduleDate").descending());

        // Pass Clinic ID
        return doctorBillRepo.searchPayouts(
                doctorId,
                status,
                startDate,
                endDate,
                TenantContext.getCurrentTenant(),
                pageable
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BulkPaymentResponseDTO payDoctorsBulk(BulkPaymentRequestDTO request, String createdBy) {

        Integer clinicId = TenantContext.getCurrentTenant();
        List<Integer> ids = request.getPaymentIds();

        if (ids == null || ids.isEmpty()) {
            throw new ValidationException("No selection", "EMPTY", HttpStatus.BAD_REQUEST);
        }

        List<DoctorSchedulePayment> bills = doctorBillRepo.findAllById(ids);

        // Check if all requested IDs were found
        if (bills.size() != ids.size()) {
            throw new ValidationException("One or more Bill IDs not found", "NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        Long referenceDoctorId = null;
        BigDecimal calculatedTotal = BigDecimal.ZERO;

        for (DoctorSchedulePayment bill : bills) {

            // SECURITY: Ensure bill belongs to current clinic
            if (!Objects.equals(bill.getClinicId(), clinicId)) {
                throw new ValidationException("Access Denied for Bill ID: " + bill.getId(), "UNAUTHORIZED", HttpStatus.FORBIDDEN);
            }

            if (bill.getStatus() == DoctorSchedulePayment.PayoutStatus.PAID) {
                throw new ValidationException(
                        "Schedule #" + bill.getDoctorSchedule().getId() + " is already paid.",
                        "ALREADY_PAID",
                        HttpStatus.CONFLICT
                );
            }

            // Check Doctor Consistency
            Long currentDoctorId = bill.getDoctorSchedule().getDoctor().getId();
            if (referenceDoctorId == null) {
                referenceDoctorId = currentDoctorId;
            } else {
                if (!currentDoctorId.equals(referenceDoctorId)) {
                    throw new ValidationException(
                            "Multi-Doctor Payment Not Allowed. Please select one doctor at a time.",
                            "MIXED_DOCTORS",
                            HttpStatus.BAD_REQUEST
                    );
                }
            }

            calculatedTotal = calculatedTotal.add(bill.getFinalPayableAmount());
        }

        if (request.getTotalAmountToPay() != null && calculatedTotal.compareTo(request.getTotalAmountToPay()) != 0) {
            throw new ValidationException(
                    "Total mismatch! System: " + calculatedTotal + ", Requested: " + request.getTotalAmountToPay(),
                    "AMOUNT_MISMATCH",
                    HttpStatus.BAD_REQUEST
            );
        }

        // --- PRE-FETCH PAID STATUS ---
        PayoutStatusLookup paidStatus = payoutStatusRepo.findByCode("PAID")
                .orElseThrow(() -> new ValidationException("System Error: Payout Status 'PAID' not found", "CONFIG_ERROR"));


        int count = 0;
        List<DoctorSchedulePayment> billsToSave = new ArrayList<>();
        List<DoctorSchedule> schedulesToSave = new ArrayList<>();

        for (DoctorSchedulePayment bill : bills) {

            // Create Transaction Record
            PayoutTransaction tx = new PayoutTransaction();
            tx.setDoctorPayment(bill);
            tx.setAmountPaid(bill.getFinalPayableAmount());
            tx.setPaymentMethod(request.getPaymentMethod());
            tx.setTransactionReference(request.getTransactionReference());
            tx.setCreatedBy(createdBy);
            tx.setClinicId(clinicId);

            bill.setStatus(DoctorSchedulePayment.PayoutStatus.PAID);

            if (bill.getTransactions() == null) bill.setTransactions(new ArrayList<>());
            bill.getTransactions().add(tx);

            billsToSave.add(bill);

            DoctorSchedule schedule = bill.getDoctorSchedule();
            schedule.setPayoutStatus(paidStatus); // <--- Set Entity
            schedulesToSave.add(schedule);

            count++;
        }

        doctorBillRepo.saveAll(billsToSave);
        scheduleRepo.saveAll(schedulesToSave);

        BulkPaymentResponseDTO response = new BulkPaymentResponseDTO();
        response.setSuccessCount(count);
        response.setTotalProcessedAmount(calculatedTotal);
        response.setMessage("Validation passed. Successfully paid " + count + " schedules.");

        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void reversePayment(Integer payoutId, String modifiedBy) {

        DoctorSchedulePayment bill = doctorBillRepo.findById(payoutId)
                .orElseThrow(() -> new ValidationException("Bill not found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!Objects.equals(bill.getClinicId(), TenantContext.getCurrentTenant())) {
            throw new ValidationException("Bill not found", "NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        if (bill.getStatus() != DoctorSchedulePayment.PayoutStatus.PAID) {
            throw new ValidationException("Only PAID bills can be reversed", "INVALID_STATUS", HttpStatus.BAD_REQUEST);
        }

        bill.setStatus(DoctorSchedulePayment.PayoutStatus.PENDING);
        bill.setUpdatedBy(modifiedBy);
        bill.setUpdatedDate(LocalDateTime.now());

        bill.getTransactions().clear();

        // --- NEW: Revert Schedule Status ---
        // We revert the Schedule's payout status back to 'PENDING'
        PayoutStatusLookup pendingPayout = payoutStatusRepo.findByCode("PENDING")
                .orElseThrow(() -> new ValidationException("System Error: Status 'PENDING' not found", "CONFIG_ERROR"));

        DoctorSchedule schedule = bill.getDoctorSchedule();
        schedule.setPayoutStatus(pendingPayout);
        scheduleRepo.save(schedule);

        doctorBillRepo.save(bill);
    }

    @Override
    public OperationalExpense addExpense(ExpenseCreateDTO dto, String createdBy) {

        Integer clinicId = TenantContext.getCurrentTenant();

        ExpenseCategory cat = categoryRepo.findById(dto.getExpenseCategoryId())
                .orElseThrow(() -> new ValidationException("Invalid Category ID", "INVALID_CAT", HttpStatus.BAD_REQUEST));

        OperationalExpense exp = new OperationalExpense();
        exp.setClinicId(clinicId);
        exp.setExpenseCategory(cat);
        exp.setDescription(dto.getDescription());
        exp.setAmount(dto.getAmount());
        exp.setDueDate(dto.getDueDate());
        exp.setStatus(DoctorSchedulePayment.PayoutStatus.PENDING);
        exp.setCreatedBy(createdBy);

        return expenseRepo.save(exp);
    }
}