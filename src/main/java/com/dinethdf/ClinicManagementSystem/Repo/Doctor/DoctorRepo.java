package com.dinethdf.ClinicManagementSystem.Repo.Doctor;

import com.dinethdf.ClinicManagementSystem.Model.Doctor.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepo extends JpaRepository<Doctor, Long> {

}


