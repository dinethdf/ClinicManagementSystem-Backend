package com.dinethdf.ClinicManagementSystem.Model.DoctorShedule;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "payout_status_lookup")
@Data
public class PayoutStatusLookup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String code; // e.g., "PENDING", "PAID"

    @Column(nullable = false, length = 50)
    private String displayName;

    @Column(name = "colorCode", length = 20)
    private String colorCode;

    @Column(name = "sortOrder")
    private Integer sortOrder;
}