package com.dinethdf.ClinicManagementSystem.Repo.Patient;

import com.dinethdf.ClinicManagementSystem.Model.Patient.PatientAllergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PatientAllergyRepo extends JpaRepository<PatientAllergy, Integer> {
    List<PatientAllergy> findByPatientIdAndClinicId(Integer patientId, Integer clinicId);
}