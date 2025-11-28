package com.dinethdf.ClinicManagementSystem.Model.Invoice; // Adjust package as needed

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "item_types")
public class ItemType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String typeName;

    private String description;
}