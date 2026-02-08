package com.dinethdf.ClinicManagementSystem.Model.Payment;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "payment_location_lookup")
public class PaymentLocationLookup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;        // 'CLINIC_COUNTER'
    private String displayName; // 'Clinic Counter'
}