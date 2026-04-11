package com.dinethdf.ClinicManagementSystem.Service.Financial;

import com.dinethdf.ClinicManagementSystem.DTO.Financial.ScheduleFinancialDTO;
import com.dinethdf.ClinicManagementSystem.Repo.Financial.FinancialRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Projection.ScheduleFinancialProjection;
import com.dinethdf.ClinicManagementSystem.config.TenantContext; // <--- Import Context
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialService {

    private final FinancialRepo financialRepo;

    @Transactional(readOnly = true)
    public List<ScheduleFinancialDTO> getFinancialsForSchedule(Long scheduleId) {

        // Pass Tenant ID to Repository
        List<ScheduleFinancialProjection> rawData = financialRepo.getScheduleFinancials(
                scheduleId,
                TenantContext.getCurrentTenant()
        );

        return rawData.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ScheduleFinancialDTO mapToDTO(ScheduleFinancialProjection proj) {
        return ScheduleFinancialDTO.builder()
                // 1. Appointment & Patient Details
                .appointmentId(proj.getAppointmentID())
                .queueNo(proj.getQueueNo())
                .patientName(proj.getPatientName())
                .patientMobile(proj.getPatientMobile())
                .invoiceId(proj.getInvoiceID())

                // 2. Status Information
                .paymentStatus(proj.getPaymentStatus())
                .paymentStatusCode(proj.getPaymentStatusCode())
                .paymentStatusColor(proj.getPaymentStatusColor())
                .appointmentStatus(proj.getAppointmentStatus())
                .appointmentStatusCode(proj.getAppointmentStatusCode())
                .appointmentStatusColor(proj.getAppointmentStatusColor())

                // 3. Base Fees
                .baseDoctorFee(proj.getBaseDoctorFee())
                .baseClinicFee(proj.getBaseClinicFee())

                // 4. Doctor Side Calculations
                .doctorDiscountAmount(proj.getDoctorDiscountAmount())
                .netDoctorPayout(proj.getNetDoctorPayout())

                // 5. Clinic Side Calculations
                .clinicDiscountAmount(proj.getClinicDiscountAmount())
                .netClinicProfit(proj.getNetClinicProfit())

                // 6. Total Bill & Payment Tracking
                .subtotalAmount(proj.getSubtotalAmount())
                .totalDiscount(proj.getTotalDiscount())
                .totalPaidByPatient(proj.getTotalPaidByPatient())
                .amountPaid(proj.getAmountPaid())
                .balanceRemaining(proj.getBalanceRemaining())
                .paymentDueDate(proj.getPaymentDueDate())
                .paymentPercentage(proj.getPaymentPercentage())

                // 7. Last Payment Details
                .lastPaymentMethod(proj.getLastPaymentMethod())
                .lastPaymentMethodCode(proj.getLastPaymentMethodCode())
                .lastPaymentLocation(proj.getLastPaymentLocation())
                .lastPaymentDate(proj.getLastPaymentDate())
                .totalPayments(proj.getTotalPayments())

                .build();
    }
}