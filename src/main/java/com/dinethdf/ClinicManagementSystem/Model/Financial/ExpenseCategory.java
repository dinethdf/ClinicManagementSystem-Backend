package com.dinethdf.ClinicManagementSystem.Model.Financial;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "expense_categories")
@Data
public class ExpenseCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    private String description;

    private Boolean isActive = true;
}