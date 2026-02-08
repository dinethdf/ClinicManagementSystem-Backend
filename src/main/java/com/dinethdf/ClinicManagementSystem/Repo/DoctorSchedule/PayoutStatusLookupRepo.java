package com.dinethdf.ClinicManagementSystem.Repo.DoctorSchedule;

import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.PayoutStatusLookup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayoutStatusLookupRepo extends JpaRepository<PayoutStatusLookup, Integer> {

    // Finds status by unique code (e.g., "NOT_PAID")
    Optional<PayoutStatusLookup> findByCode(String code);
}