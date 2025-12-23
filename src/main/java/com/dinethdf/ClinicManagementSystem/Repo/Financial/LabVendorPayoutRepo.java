package com.dinethdf.ClinicManagementSystem.Repo.Financial;

import com.dinethdf.ClinicManagementSystem.Model.Financial.LabVendorPayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabVendorPayoutRepo extends JpaRepository<LabVendorPayout, Integer> {
}