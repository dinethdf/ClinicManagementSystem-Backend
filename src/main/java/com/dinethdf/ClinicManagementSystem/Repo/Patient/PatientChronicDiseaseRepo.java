package com.dinethdf.ClinicManagementSystem.Repo.Patient;

import com.dinethdf.ClinicManagementSystem.Model.Patient.PatientChronicDisease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PatientChronicDiseaseRepo extends JpaRepository<PatientChronicDisease, Integer> {
    List<PatientChronicDisease> findByPatientIdAndClinicId(Integer patientId, Integer clinicId);
}