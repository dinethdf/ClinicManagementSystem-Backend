package com.dinethdf.ClinicManagementSystem.Repo.Clinic;


import com.dinethdf.ClinicManagementSystem.Model.Clinic.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Integer> {

    // Find by public clinic code
    Optional<Clinic> findByClinicCode(String clinicCode);

    // Find by subdomain (for tenant resolution)
    Optional<Clinic> findBySubdomain(String subdomain);

    // Check if subdomain exists (for validation)
    boolean existsBySubdomain(String subdomain);

    // Find by Code but ensure it's not deleted
    Optional<Clinic> findByClinicCodeAndDeletedFalse(String clinicCode);
}