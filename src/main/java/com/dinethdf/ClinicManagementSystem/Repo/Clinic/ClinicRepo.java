package com.dinethdf.ClinicManagementSystem.Repo.Clinic;

import com.dinethdf.ClinicManagementSystem.Model.Clinic.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClinicRepo extends JpaRepository<Clinic, Integer> {
    Optional<Clinic> findBySubdomain(String subdomain);
}