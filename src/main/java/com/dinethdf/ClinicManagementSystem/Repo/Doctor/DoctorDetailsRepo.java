package com.dinethdf.ClinicManagementSystem.Repo.Doctor;

import com.dinethdf.ClinicManagementSystem.Model.Doctor.DoctorDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorDetailsRepo extends JpaRepository<DoctorDetails, Long> {

}


