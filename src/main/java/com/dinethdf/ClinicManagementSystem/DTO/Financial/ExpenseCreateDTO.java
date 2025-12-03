package com.dinethdf.ClinicManagementSystem.DTO.Financial;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseCreateDTO {
    // We now send the ID (e.g., 1 for RENT, 2 for UTILITY)
    private Integer expenseCategoryId;

    private String description;
    private BigDecimal amount;
    private LocalDate dueDate;
}