package com.dinethdf.ClinicManagementSystem.Repo.Patient;

import com.dinethdf.ClinicManagementSystem.Model.Patient.PatientDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientDetailsRepo extends JpaRepository<PatientDetails, Long> {

}


