package com.dinethdf.ClinicManagementSystem.Repo.Doctor;

import com.dinethdf.ClinicManagementSystem.Model.Doctor.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecializationRepo extends JpaRepository<Specialization, Integer> {

    // 1. Search Query: Matches "Gene" to "General Physician", "Geneticist", etc.
    List<Specialization> findByNameContainingIgnoreCase(String name);

    // 2. Validation: Check if name exists (for Create)
    boolean existsByNameIgnoreCase(String name);

    // 3. Validation: Check if name exists but ignore current ID (for Update)
    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);
}