package com.dinethdf.ClinicManagementSystem.Repo.Payout;

import com.dinethdf.ClinicManagementSystem.Model.Financial.OperationalExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface OperationalExpenseRepo extends JpaRepository<OperationalExpense, Integer> {
    // Standard CRUD is sufficient
    @Query("""
        SELECT COALESCE(SUM(e.amount), 0) 
        FROM OperationalExpense e 
        WHERE (:startDate IS NULL OR e.dueDate >= :startDate)
        AND (:endDate IS NULL OR e.dueDate <= :endDate)
    """)
    BigDecimal sumExpensesByDate(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}