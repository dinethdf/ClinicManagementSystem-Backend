package com.dinethdf.ClinicManagementSystem.Repo.Financial;

import com.dinethdf.ClinicManagementSystem.Model.Financial.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseCategoryRepo extends JpaRepository<ExpenseCategory, Integer> {
}