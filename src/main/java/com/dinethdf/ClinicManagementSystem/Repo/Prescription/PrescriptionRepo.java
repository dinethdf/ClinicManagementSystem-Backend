package com.dinethdf.ClinicManagementSystem.Repo.Prescription;

import com.dinethdf.ClinicManagementSystem.Model.Prescription.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrescriptionRepo extends JpaRepository<Prescription, Long> {

    // Find prescription by appointment ID
    Optional<Prescription> findByAppointmentId(Long appointmentId);
}