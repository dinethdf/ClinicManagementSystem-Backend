package com.dinethdf.ClinicManagementSystem.Model.Invoice;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoice_status_lookup")
public class InvoiceStatusLookup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Using Integer since DB is TINYINT/INT

    @Column(nullable = false, unique = true, length = 30)
    private String code; // e.g., 'PAID', 'UNPAID'

    @Column(name = "displayName", nullable = false, length = 50)
    private String displayName; // e.g., 'Paid', 'Unpaid'

    @Column(name = "colorCode", length = 20)
    private String colorCode; // e.g., '#28A745'

    @Column(name = "sortOrder")
    private Integer sortOrder;

    @Column(name = "createdDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }
}