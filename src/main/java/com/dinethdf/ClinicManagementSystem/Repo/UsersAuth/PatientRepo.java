package com.dinethdf.ClinicManagementSystem.Repo.UsersAuth;

import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.PatientAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepo extends JpaRepository<PatientAuth, Integer> {

    PatientAuth findByEmailAndClinicId(String email, Integer clinicId);
}
