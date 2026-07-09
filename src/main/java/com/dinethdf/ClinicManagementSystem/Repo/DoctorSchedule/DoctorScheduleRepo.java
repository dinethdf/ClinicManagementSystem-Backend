package com.dinethdf.ClinicManagementSystem.Repo.DoctorSchedule;

import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.DoctorSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface DoctorScheduleRepo extends JpaRepository<DoctorSchedule, Long>, JpaSpecificationExecutor<DoctorSchedule> {

    boolean existsByIdAndDoctorIdAndClinicId(Long id, Long doctorId, Integer clinicId);

    // Find all schedules for a specific doctor (Scoped to Clinic)
    Page<DoctorSchedule> findByDoctorIdAndClinicId(Long doctorId, Integer clinicId, Pageable pageable);

    // 2. Find ONLY ACTIVE with Pagination
    // We hardcode the sort here to ensure "Latest to Oldest" is always enforced for this specific query
    @Query("""
        SELECT s FROM DoctorSchedule s
        WHERE s.clinicId = :clinicId
          AND s.doctor.id = :doctorId
          AND s.status.code = 'ACTIVE'
        ORDER BY s.scheduleDate DESC, s.startTime DESC
    """)
    Page<DoctorSchedule> findActiveByDoctorId(@Param("doctorId") Long doctorId,
                                              @Param("clinicId") Integer clinicId,
                                              Pageable pageable);

    // Find all ACTIVE schedules (Scoped to Clinic)
    // UPDATED: s.status.code
    @Query("SELECT s FROM DoctorSchedule s WHERE s.clinicId = :clinicId AND s.status.code = 'ACTIVE'")
    List<DoctorSchedule> findAllActive(@Param("clinicId") Integer clinicId);

    // Find ACTIVE schedules for a doctor on or after a certain date
    // UPDATED: s.status.code
    @Query("SELECT s FROM DoctorSchedule s WHERE s.clinicId = :clinicId AND s.doctor.id = :doctorId AND s.status.code = 'ACTIVE' AND s.scheduleDate >= :date")
    List<DoctorSchedule> findActiveByDoctorIdAndScheduleDateAfter(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("clinicId") Integer clinicId
    );

    // Upcoming schedules for a doctor
    @Query("SELECT s FROM DoctorSchedule s " +
            "WHERE s.clinicId = :clinicId " +
            "AND s.doctor.id = :doctorId " +
            "AND s.status.code = 'ACTIVE' " +
            "AND s.isBookingOpen = true " +
            "AND ( (s.scheduleDate = :today AND s.endTime > :now) OR (s.scheduleDate > :today) )")
    List<DoctorSchedule> findUpcomingSchedulesByDoctorId(
            @Param("doctorId") Long doctorId,
            @Param("today") LocalDate today,
            @Param("now") LocalTime now,
            @Param("clinicId") Integer clinicId
    );

    // Paginated list with ID filters + Date Range
    @Query("SELECT s FROM DoctorSchedule s " +
            "JOIN FETCH s.doctor d " +
            "JOIN FETCH d.specialization sp " +
            "WHERE s.clinicId = :clinicId " +
            "AND (:doctorId IS NULL OR d.id = :doctorId) " +
            "AND (:specializationId IS NULL OR sp.id = :specializationId) " +
            "AND (:startDate IS NULL OR s.scheduleDate >= :startDate) " +
            "AND (:endDate IS NULL OR s.scheduleDate <= :endDate) " +
            "ORDER BY CASE WHEN s.scheduleDate = :today THEN 1 WHEN s.scheduleDate > :today THEN 2 ELSE 3 END ASC, s.scheduleDate ASC, s.startTime ASC")
    Page<DoctorSchedule> findAllSchedules(
            @Param("today") LocalDate today,
            @Param("doctorId") Long doctorId,
            @Param("specializationId") Integer specializationId,
            @Param("startDate") LocalDate startDate, // New
            @Param("endDate") LocalDate endDate,     // New
            @Param("clinicId") Integer clinicId,
            Pageable pageable
    );

    // Paginated upcoming schedules with filters — uses JOIN FETCH to prevent N+1 queries
    // JOIN FETCH eagerly loads status, payoutStatus, doctor and specialization in ONE SQL query
    @Query(value = "SELECT DISTINCT s FROM DoctorSchedule s " +
            "JOIN FETCH s.doctor d " +
            "JOIN FETCH d.specialization sp " +
            "JOIN FETCH s.status st " +
            "LEFT JOIN FETCH s.payoutStatus ps " +
            "WHERE s.clinicId = :clinicId " +
            "AND st.code = 'ACTIVE' " +
            "AND s.isBookingOpen = true " +
            "AND ( (s.scheduleDate = :today AND s.endTime > :now) OR (s.scheduleDate > :today) ) " +
            "AND (:doctorName IS NULL OR LOWER(d.firstName) LIKE LOWER(CONCAT('%', :doctorName, '%')) OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :doctorName, '%'))) " +
            "AND (:specialization IS NULL OR LOWER(sp.name) LIKE LOWER(CONCAT('%', :specialization, '%'))) " +
            "AND (:searchDate IS NULL OR s.scheduleDate = :searchDate)",
           countQuery = "SELECT COUNT(DISTINCT s) FROM DoctorSchedule s " +
            "JOIN s.doctor d " +
            "JOIN d.specialization sp " +
            "JOIN s.status st " +
            "WHERE s.clinicId = :clinicId " +
            "AND st.code = 'ACTIVE' " +
            "AND s.isBookingOpen = true " +
            "AND ( (s.scheduleDate = :today AND s.endTime > :now) OR (s.scheduleDate > :today) ) " +
            "AND (:doctorName IS NULL OR LOWER(d.firstName) LIKE LOWER(CONCAT('%', :doctorName, '%')) OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :doctorName, '%'))) " +
            "AND (:specialization IS NULL OR LOWER(sp.name) LIKE LOWER(CONCAT('%', :specialization, '%'))) " +
            "AND (:searchDate IS NULL OR s.scheduleDate = :searchDate)")
    Page<DoctorSchedule> findUpcomingSchedules(
            @Param("today") LocalDate today,
            @Param("now") LocalTime now,
            @Param("doctorName") String doctorName,
            @Param("specialization") String specialization,
            @Param("searchDate") LocalDate searchDate,
            @Param("clinicId") Integer clinicId,
            Pageable pageable
    );

    // Overlapping schedule check for create
    // UPDATED: s.status.code
    @Query("SELECT s FROM DoctorSchedule s " +
            "WHERE s.clinicId = :clinicId " +
            "AND s.doctor.id = :doctorId " +
            "AND s.status.code = 'ACTIVE' " +
            "AND s.startTime < :endTime " +
            "AND s.endTime > :startTime " +
            "AND s.scheduleDate = :scheduleDate")
    List<DoctorSchedule> findOverlappingSchedules(
            @Param("doctorId") Long doctorId,
            @Param("scheduleDate") LocalDate scheduleDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("clinicId") Integer clinicId
    );

    // Overlapping schedule check for update (returns the actual conflicting schedules)
    // UPDATED: s.status.code
    @Query("SELECT s FROM DoctorSchedule s " +
            "WHERE s.clinicId = :clinicId " +
            "AND s.doctor.id = :doctorId " +
            "AND s.status.code = 'ACTIVE' " +
            "AND s.scheduleDate = :scheduleDate " +
            "AND s.id != :scheduleIdToExclude " +
            "AND s.startTime < :endTime " +
            "AND s.endTime > :startTime")
    List<DoctorSchedule> findOverlappingSchedulesForUpdate(
            @Param("doctorId") Long doctorId,
            @Param("scheduleDate") LocalDate scheduleDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("scheduleIdToExclude") Long scheduleIdToExclude,
            @Param("clinicId") Integer clinicId
    );

    @org.springframework.data.jpa.repository.query.Procedure(name = "DoctorSchedule.cancel")
    java.util.Map<String, Object> sp_CancelSchedule(
            @Param("p_clinic_id") Integer clinicId,
            @Param("p_schedule_id") Long scheduleId,
            @Param("p_cancelled_by") String cancelledBy
    );
}