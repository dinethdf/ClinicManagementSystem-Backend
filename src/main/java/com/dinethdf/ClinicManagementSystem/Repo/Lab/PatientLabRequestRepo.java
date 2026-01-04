package com.dinethdf.ClinicManagementSystem.Repo.Lab;

import com.dinethdf.ClinicManagementSystem.Model.Lab.PatientLabRequest;
import com.dinethdf.ClinicManagementSystem.Model.Lab.PatientLabRequest.LabStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientLabRequestRepo extends JpaRepository<PatientLabRequest, Integer> {

    // 1. Basic Tenant-Aware Fetch
    Page<PatientLabRequest> findAllByClinicId(Integer clinicId, Pageable pageable);

    // 2. Fetch History for a Specific Patient
    @Query("SELECT r FROM PatientLabRequest r WHERE r.clinicId = :clinicId AND r.patient.id = :patientId")
    Page<PatientLabRequest> findByPatientIdAndClinicId(@Param("patientId") Integer patientId,
                                                       @Param("clinicId") Integer clinicId,
                                                       Pageable pageable);

    // 3. Search (Admin/Technician View)
    @Query("""
        SELECT r FROM PatientLabRequest r
        JOIN r.patient p
        JOIN r.labTestType t
        WHERE r.clinicId = :clinicId
          AND (:status IS NULL OR r.status = :status)
          AND (:search IS NULL OR
               LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<PatientLabRequest> searchRequests(@Param("clinicId") Integer clinicId,
                                           @Param("search") String search,
                                           @Param("status") LabStatus status,
                                           Pageable pageable);

    // 4. Check usage (Prevents deleting a Test Type that is in use)
    // NOTE: This checks the REQUEST table to see if a TYPE is used
    boolean existsByLabTestTypeIdAndClinicId(Integer labTestTypeId, Integer clinicId);
}