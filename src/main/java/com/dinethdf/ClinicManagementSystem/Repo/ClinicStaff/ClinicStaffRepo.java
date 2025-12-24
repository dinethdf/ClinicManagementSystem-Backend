package com.dinethdf.ClinicManagementSystem.Repo.ClinicStaff;

import com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff.ClinicStaffResponseDto;
import com.dinethdf.ClinicManagementSystem.Model.ClinicStaff.ClinicStaff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClinicStaffRepo extends JpaRepository<ClinicStaff, Long> {

    // These now check duplicates ONLY within the specific clinic
    boolean existsByUserAuthEmailAndClinicId(String email, Integer clinicId);
    boolean existsByNicAndClinicId(String nic, Integer clinicId);

    // For Edit: Check duplicates in this clinic, excluding the current staff ID
    boolean existsByUserAuthEmailAndIdNotAndClinicId(String email, Long id, Integer clinicId);
    boolean existsByNicAndIdNotAndClinicId(String nic, Long id, Integer clinicId);


    @Query("""
    SELECT new com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff.ClinicStaffResponseDto(
        s.id, s.firstName, s.lastName, s.email, s.mobileNo, s.city,
        s.nic, s.dob, s.nameWithInitials, s.designation, s.status, s.profileImg , s.gender
    )
    FROM ClinicStaff s
    WHERE s.clinicId = :clinicId
    """)
    Page<ClinicStaffResponseDto> findAllStaffPaginated(@Param("clinicId") Integer clinicId, Pageable pageable);

    @Query("""
       SELECT new com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff.ClinicStaffResponseDto(
           s.id, s.firstName, s.lastName, s.email, s.mobileNo, s.city,
           s.nic, s.dob, s.nameWithInitials, s.designation, s.status, s.profileImg, s.gender
       )
       FROM ClinicStaff s
       WHERE s.id = :id AND s.clinicId = :clinicId
    """)
    Optional<ClinicStaffResponseDto> findStaffDetailByIdAndClinicId(@Param("id") Long id, @Param("clinicId") Integer clinicId);
}