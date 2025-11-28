package com.dinethdf.ClinicManagementSystem.Model.Invoice;

import com.dinethdf.ClinicManagementSystem.Model.AuditWrapper;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "invoice_items")
public class InvoiceItem extends AuditWrapper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_type_id", nullable = false)
    private ItemType itemType;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal amount;
}