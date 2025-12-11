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

    boolean existsByUserAuthEmail(String email);
    boolean existsByNic(String nic);

    boolean existsByUserAuthEmailAndIdNot(String email, Long id);
    boolean existsByNicAndIdNot(String nic, Long id);


    @Query("""
    SELECT new com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff.ClinicStaffResponseDto(
        s.id, s.firstName, s.lastName, s.email, s.mobileNo, s.city,
        s.nic, s.dob, s.nameWithInitials, s.designation, s.status, s.profileImg , s.gender
    )
    FROM ClinicStaff s
    """)
    Page<ClinicStaffResponseDto> findAllStaffPaginated(Pageable pageable);

    @Query("""
       SELECT new com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff.ClinicStaffResponseDto(
           s.id, s.firstName, s.lastName, s.email, s.mobileNo, s.city,
           s.nic, s.dob, s.nameWithInitials, s.designation, s.status, s.profileImg, s.gender
       )
       FROM ClinicStaff s
       WHERE s.id = :id
    """)
    Optional<ClinicStaffResponseDto> findStaffDetailById(@Param("id") Long id);
}
