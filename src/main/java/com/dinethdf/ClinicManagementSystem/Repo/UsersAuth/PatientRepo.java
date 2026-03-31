package com.dinethdf.ClinicManagementSystem.Repo.UsersAuth;

import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.PatientAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepo extends JpaRepository<PatientAuth, Integer> {

    PatientAuth findByEmailAndClinicId(String email, Integer clinicId);

    Optional<PatientAuth> findByDetailsIdAndClinicId(Long i, Integer currentTenant);
}
