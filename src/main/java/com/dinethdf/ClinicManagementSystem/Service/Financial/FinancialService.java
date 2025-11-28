package com.dinethdf.ClinicManagementSystem.Service.Financial;

import com.dinethdf.ClinicManagementSystem.DTO.Financial.ScheduleFinancialDTO;
import com.dinethdf.ClinicManagementSystem.Repo.Financial.FinancialRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Projection.ScheduleFinancialProjection;
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
    public List<ScheduleFinancialDTO> getFinancialsForSchedule(Integer scheduleId) {

        System.out.println("run this");
        List<ScheduleFinancialProjection> rawData = financialRepo.getScheduleFinancials(scheduleId);

        return rawData.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ScheduleFinancialDTO mapToDTO(ScheduleFinancialProjection proj) {
        return new ScheduleFinancialDTO(
                proj.getAppointmentID(),
                proj.getQueueNo(),
                proj.getPatientName(),
                proj.getPatientMobile(),
                proj.getInvoiceID(),
                proj.getPaymentStatus(),
                proj.getBaseDoctorFee(),
                proj.getBaseClinicFee(),
                proj.getDoctorDiscountAmount(),
                proj.getNetDoctorPayout(),
                proj.getClinicDiscountAmount(),
                proj.getNetClinicProfit(),
                proj.getTotalPaidByPatient()
        );
    }
}