package com.dinethdf.ClinicManagementSystem.Repo.Clinic;

import com.dinethdf.ClinicManagementSystem.Model.Clinic.Clinic;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.ClinicView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicViewRepo extends JpaRepository<ClinicView, Integer> {
}