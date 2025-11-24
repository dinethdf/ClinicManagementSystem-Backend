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
import java.util.Optional; // <-- Import this

@Repository
public interface DoctorRepo extends JpaRepository<Doctor, Long> {

    // Cant Use Find all Because USe Lazy load then it work Only this way
    @Query("SELECT d FROM Doctor d LEFT JOIN FETCH d.specialization")
    List<Doctor> findAllWithSpecialization();

    /**
     * Finds a Doctor entity by joining with the UserAuth entity
     */
    Optional<Doctor> findByUserAuthEmail(String email);

    @Query("""
    SELECT new com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorResponseDto(
        d.id, d.firstName, d.lastName, d.email, d.mobileNo, d.city, d.nic, d.dob,
        d.nameWithInitials, d.slmcNumber, d.specialization,
        d.qualification, d.experienceYears, d.profileImg
    )
    FROM Doctor d
    LEFT JOIN d.specialization s
    WHERE
        LOWER(d.nameWithInitials) LIKE LOWER(CONCAT('%', :query, '%')) OR
        LOWER(CONCAT(d.firstName, ' ', d.lastName)) LIKE LOWER(CONCAT('%', :query, '%'))
""")
    List<DoctorResponseDto> searchDoctors(@Param("query") String query);

    @Query("""
    SELECT new com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorResponseDto(
        d.id, d.firstName, d.lastName, d.email, d.mobileNo, d.city, d.nic, d.dob,
        d.nameWithInitials, d.slmcNumber, d.specialization,
        d.qualification, d.experienceYears, d.profileImg
    )
    FROM Doctor d
    LEFT JOIN d.specialization s
    """)
    Page<DoctorResponseDto> findAllDoctorsPaginated(Pageable pageable);
}


