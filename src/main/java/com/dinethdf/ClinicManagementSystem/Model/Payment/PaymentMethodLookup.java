package com.dinethdf.ClinicManagementSystem.Model.Payment;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "payment_method_lookup")
public class PaymentMethodLookup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;        // 'CASH'
    private String displayName; // 'Cash'
}