package com.dinethdf.ClinicManagementSystem.Repo;

import com.dinethdf.ClinicManagementSystem.Model.Users.Patient;
import com.dinethdf.ClinicManagementSystem.Model.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepo extends JpaRepository<Patient, Integer> {

    Patient findByEmail(String email);
}
