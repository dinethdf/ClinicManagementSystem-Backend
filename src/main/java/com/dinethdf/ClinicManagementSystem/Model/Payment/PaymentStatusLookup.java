package com.dinethdf.ClinicManagementSystem.Model.Payment;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "payment_status_lookup")
public class PaymentStatusLookup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;        // 'COMPLETED'
    private String displayName; // 'Completed'
    private String colorCode;   // '#28A745'
}
