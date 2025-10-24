package com.dinethdf.ClinicManagementSystem.Repo.Patient;

import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientDetailsRepo extends JpaRepository<Patient, Long> {


    // ✅ Option 1: Count number of dependents by guardian
    long countByGuardian(Patient guardian);

    // For Future reff
    //  Get the list of dependents for this guardian
    List<Patient> findByGuardian(Patient guardian);

}


