package com.dinethdf.ClinicManagementSystem.Repo.Invoice;

import com.dinethdf.ClinicManagementSystem.Model.Invoice.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepo extends JpaRepository<Invoice, Long> {
    // We need this to get the invoice
}
