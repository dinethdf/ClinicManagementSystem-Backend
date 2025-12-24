package com.dinethdf.ClinicManagementSystem.Repo.Invoice;

import com.dinethdf.ClinicManagementSystem.Model.Invoice.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepo extends JpaRepository<Invoice, Long> {

    // Helper to find invoice by ID AND Clinic (Security check)
    Optional<Invoice> findByIdAndClinicId(Long id, Integer clinicId);
}