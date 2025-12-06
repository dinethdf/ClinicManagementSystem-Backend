package com.dinethdf.ClinicManagementSystem.DTO.Financial;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class FinancialSummaryDTO {

    private BigDecimal totalPaidToDoctors;
    private BigDecimal totalPendingDoctors;
    private BigDecimal totalOperationalExpenses;

    // --- MANUAL CONSTRUCTOR (Add this to fix the error) ---
    public FinancialSummaryDTO(BigDecimal totalPaidToDoctors, BigDecimal totalPendingDoctors, BigDecimal totalOperationalExpenses) {
        this.totalPaidToDoctors = totalPaidToDoctors;
        this.totalPendingDoctors = totalPendingDoctors;
        this.totalOperationalExpenses = totalOperationalExpenses;
    }
}