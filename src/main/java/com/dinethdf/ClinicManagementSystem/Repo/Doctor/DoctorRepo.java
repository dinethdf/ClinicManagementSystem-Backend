package com.dinethdf.ClinicManagementSystem.Repo.Doctor;

import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorResponseDto;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepo extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByIdAndClinicId(Long aLong, Integer clinicId);

    boolean existsByUserAuthEmailAndClinicIdAndIdNot(String email, Integer clinicId, Long id);
    boolean existsByNicAndClinicIdAndIdNot(String nic, Integer clinicId, Long id);

    boolean existsBySlmcNumberAndClinicIdAndIdNot(String slmcNumber, Integer clinicId, Long id);

    @Query("SELECT d FROM Doctor d LEFT JOIN FETCH d.specialization WHERE d.clinicId = :clinicId")
    List<Doctor> findAllWithSpecialization(@Param("clinicId") Integer clinicId);

    Optional<Doctor> findByUserAuthEmailAndClinicId(String email, Integer clinicId);

    @Query("""
    SELECT new com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorResponseDto(
        d.id, d.firstName, d.lastName, d.email, d.mobileNo, d.city, d.nic, d.dob,
        d.nameWithInitials, d.slmcNumber, d.specialization,
        d.qualification, d.experienceYears, d.profileImg
    )
    FROM Doctor d
    LEFT JOIN d.specialization s
    WHERE d.clinicId = :clinicId
      AND (LOWER(d.nameWithInitials) LIKE LOWER(CONCAT('%', :query, '%')) OR
           LOWER(CONCAT(d.firstName, ' ', d.lastName)) LIKE LOWER(CONCAT('%', :query, '%')))
""")
    List<DoctorResponseDto> searchDoctors(@Param("query") String query, @Param("clinicId") Integer clinicId);

    @Query("""
    SELECT new com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorResponseDto(
        d.id, d.firstName, d.lastName, d.email, d.mobileNo, d.city, d.nic, d.dob,
        d.nameWithInitials, d.slmcNumber, d.specialization,
        d.qualification, d.experienceYears, d.profileImg
    )
    FROM Doctor d
    LEFT JOIN d.specialization s
    WHERE d.clinicId = :clinicId
    """)
    Page<DoctorResponseDto> findAllDoctorsPaginated(Pageable pageable, @Param("clinicId") Integer clinicId);
}