package com.dinethdf.ClinicManagementSystem.Repo.Appointment;

import com.dinethdf.ClinicManagementSystem.Model.Appointment.AppointmentStatusLookup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppointmentStatusLookupRepo extends JpaRepository<AppointmentStatusLookup, Integer> {
    Optional<AppointmentStatusLookup> findByCode(String code);
}