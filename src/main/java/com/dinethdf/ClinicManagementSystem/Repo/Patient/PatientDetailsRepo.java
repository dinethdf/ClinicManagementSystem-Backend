package com.dinethdf.ClinicManagementSystem.Repo.Patient;

import com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff.ClinicStaffResponseDto;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.GuardianResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientListDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientSimpleResponseDTO;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientDetailsRepo extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {

    // Tenant-scoped full list — used by getAllPatients() to prevent cross-tenant leaks
    List<Patient> findByClinicId(Integer clinicId);

    long countByGuardian(Patient guardian);

    @Query("""
    SELECT new com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientListDTO(
        p.id,
        p.patientNo,
        p.firstName,
        p.lastName,
        p.mobileNo,
        p.nic,
        p.city,
        p.dob,
        p.isActive
    )
    FROM Patient p
    WHERE p.clinicId = :clinicId
    AND (:patientNo IS NULL OR LOWER(p.patientNo) LIKE LOWER(CONCAT('%', :patientNo, '%')))
    AND (:nic IS NULL OR LOWER(p.nic) LIKE LOWER(CONCAT('%', :nic, '%')))
    AND (:mobile IS NULL OR p.mobileNo LIKE CONCAT('%', :mobile, '%'))
    AND (:name IS NULL OR (
        LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR 
        LOWER(p.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
    ))
""")
    Page<PatientListDTO> searchPatientsAdvanced(
            @Param("patientNo") String patientNo,
            @Param("nic") String nic,
            @Param("mobile") String mobile,
            @Param("name") String name,
            @Param("clinicId") Integer clinicId,
            Pageable pageable
    );

    @Query("""
    SELECT new com.dinethdf.ClinicManagementSystem.DTO.Patient.GuardianResponseDTO(
        p.id,
        p.patientNo,
        p.firstName,
        p.lastName,
        p.nic,
        p.address,
        p.city,
        p.district,
        p.email,
        p.mobileNo,
        p.landNo
    )
    FROM Patient p
    WHERE p.clinicId = :clinicId
      AND (:nic IS NULL OR LOWER(p.nic) LIKE LOWER(CONCAT('%', :nic, '%')))
      AND (:fullName IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :fullName, '%')))
      AND p.isActive = true
      AND (p.guardian IS NULL OR p.guardian.id = 0)
""")
    List<GuardianResponseDTO> searchGuardianByNicAndFullName(
            @Param("nic") String nic,
            @Param("fullName") String fullName,
            @Param("clinicId") Integer clinicId);

    @Query("""
    SELECT new com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientResponseDTO(
        p.id,
        p.patientNo,
        p.firstName,
        p.lastName,
        p.nic,
        p.address,
        p.dob,
        new com.dinethdf.ClinicManagementSystem.DTO.Patient.GuardianResponseDTO(
            g.id,
            g.patientNo,
            g.firstName,
            g.lastName,
            g.nic,
            g.address,
            g.city,
            g.district,
            g.email,
            g.mobileNo,
            g.landNo
        ),
        p.city,
        p.district,
        p.email,
        p.mobileNo,
        p.landNo,
        p.profileImg
    )
    FROM Patient p
    LEFT JOIN p.guardian g
    WHERE p.clinicId = :clinicId
      AND (:patientNo IS NULL OR LOWER(p.patientNo) LIKE LOWER(CONCAT('%', :patientNo, '%')))
      AND (:mobileNo IS NULL OR LOWER(p.mobileNo) LIKE LOWER(CONCAT('%', :mobileNo, '%')))
      AND (:nic IS NULL OR LOWER(p.nic) LIKE LOWER(CONCAT('%', :nic, '%')))
      AND (:fullName IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :fullName, '%')))
      AND p.isActive = true
""")
    List<PatientResponseDTO> searchPatient(
            @Param("patientNo") String patientNo,
            @Param("nic") String nic,
            @Param("fullName") String fullName,
            @Param("mobileNo") String mobileNo,
            @Param("clinicId") Integer clinicId);

    @Query("""
    SELECT new com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientSimpleResponseDTO(
        p.id,
        p.patientNo,
        p.firstName,
        p.lastName
    )
    FROM Patient p
    WHERE p.clinicId = :clinicId
      AND (:patientNo IS NULL OR LOWER(p.patientNo) LIKE LOWER(CONCAT('%', :patientNo, '%')))
      AND p.isActive = true
    """)
    List<PatientSimpleResponseDTO> searchByPatientNo(
            @Param("patientNo") String patientNo,
            @Param("clinicId") Integer clinicId);



    @Query("SELECT p.id FROM Patient p WHERE p.clinicId = :clinicId AND p.nic = :nic AND p.isActive = true")
    Optional<Long> findIdByNic(@Param("nic") String nic, @Param("clinicId") Integer clinicId);

    @Query("""
    SELECT new com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientResponseDTO(
        p.id,
        p.patientNo,
        p.firstName,
        p.lastName,
        p.nic,
        p.address,
        p.dob,
        null,
        p.city,
        p.district,
        p.email,
        p.mobileNo,
        p.landNo,
        p.profileImg
    )
    FROM Patient p
    WHERE p.id = :id
      AND p.clinicId = :clinicId
      AND p.isActive = true
""")
    Optional<PatientResponseDTO> findPatientDetailByIdAndClinicId(
            @Param("id") Long id,
            @Param("clinicId") Integer clinicId
    );


    @Query("""
    SELECT new com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientResponseDTO(
        p.id,
        p.patientNo,
        p.firstName,
        p.lastName,
        p.nic,
        p.address,
        p.dob,
        new com.dinethdf.ClinicManagementSystem.DTO.Patient.GuardianResponseDTO(
            g.id,
            g.patientNo,
            g.firstName,
            g.lastName,
            g.nic,
            g.address,
            g.city,
            g.district,
            g.email,
            g.mobileNo,
            g.landNo
        ),
        p.city,
        p.district,
        p.email,
        p.mobileNo,
        p.landNo,
        p.profileImg
    )
    FROM Patient p
    LEFT JOIN p.guardian g
    WHERE p.clinicId = :clinicId
      AND p.isActive = true
      AND (
          p.nic = :searchValue
          OR
          p.mobileNo = :searchValue
      )
    """)
    List<PatientResponseDTO> findByNicOrMobile(
            @Param("searchValue") String searchValue,
            @Param("clinicId") Integer clinicId
    );

}