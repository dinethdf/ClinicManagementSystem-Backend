package com.dinethdf.ClinicManagementSystem.Repo.Clinic;

import com.dinethdf.ClinicManagementSystem.Model.Clinic.ClinicFeatureView;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClinicFeatureViewRepo extends JpaRepository<ClinicFeatureView, Integer> {

    // Fetch all features for a specific clinic
    List<ClinicFeatureView> findAllByClinicId(Integer clinicId);
}