package com.dinethdf.ClinicManagementSystem.Model.Doctor;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="specialization")
@Data
public class Specialization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "note", nullable = true, length = 100)
    private String note;
}
