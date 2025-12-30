package com.dinethdf.ClinicManagementSystem.Repo.Lab;

import com.dinethdf.ClinicManagementSystem.Model.Lab.LabTestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LabTestTypeRepo extends JpaRepository<LabTestType, Integer> {

    // Check for duplicate NAME
    boolean existsByNameAndClinicId(String name, Integer clinicId);
    boolean existsByNameAndClinicIdAndIdNot(String name, Integer clinicId, Integer id);

    // --- NEW: Check for duplicate CODE ---
    boolean existsByCodeAndClinicId(String code, Integer clinicId);
    boolean existsByCodeAndClinicIdAndIdNot(String code, Integer clinicId, Integer id);

    // Get Active Tests
    @Query("SELECT l FROM LabTestType l WHERE l.clinicId = :clinicId AND l.isActive = true ORDER BY l.name ASC")
    Page<LabTestType> findAllActive(@Param("clinicId") Integer clinicId, Pageable pageable);

    // Search
    @Query("""
        SELECT l FROM LabTestType l
        WHERE l.clinicId = :clinicId
          AND (:search IS NULL OR LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(l.code) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<LabTestType> searchAll(@Param("search") String search,
                                @Param("clinicId") Integer clinicId,
                                Pageable pageable);
}