package com.dinethdf.ClinicManagementSystem.Repo.DoctorSchedule;

import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.ScheduleStatusLookup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScheduleStatusLookupRepo extends JpaRepository<ScheduleStatusLookup, Integer> {

    // Finds status by unique code (e.g., "ACTIVE")
    Optional<ScheduleStatusLookup> findByCode(String code);
}