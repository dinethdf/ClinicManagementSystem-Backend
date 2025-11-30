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

    // Find all schedules for a specific doctor
    List<DoctorSchedule> findByDoctorId(Long doctorId);

    // Find only active schedules for a specific doctor
    List<DoctorSchedule> findByDoctorIdAndIsActiveTrue(Long doctorId);

    // Find all active schedules
    List<DoctorSchedule> findByIsActiveTrue();

    // Find active schedules for a doctor on or after a certain date
    // call this minus day need to add
    List<DoctorSchedule> findByDoctorIdAndScheduleDateAfterAndIsActiveTrue(Long doctorId, LocalDate date);

    @Query("SELECT s FROM DoctorSchedule s " +
            "WHERE s.doctor.id = :doctorId " +
            "AND s.isActive = true " +
            "AND ( " +
            "    (s.scheduleDate = :today AND s.endTime > :now) " +
            "    OR " +
            "    (s.scheduleDate > :today) " +
            ")")
    List<DoctorSchedule> findUpcomingSchedulesByDoctorId(
            @Param("doctorId") Long doctorId,
            @Param("today") LocalDate today,
            @Param("now") LocalTime now
    );

    @Query("SELECT s FROM DoctorSchedule s " +
            "JOIN s.doctor d " +
            "JOIN d.specialization sp " +
            "WHERE s.isActive = true " +
            "AND (:doctorName IS NULL OR LOWER(d.firstName) LIKE LOWER(CONCAT('%', :doctorName, '%')) OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :doctorName, '%'))) " +
            "AND (:specialization IS NULL OR LOWER(sp.name) LIKE LOWER(CONCAT('%', :specialization, '%'))) " +
            "AND (:searchDate IS NULL OR s.scheduleDate = :searchDate) " +
            "ORDER BY " +
            "CASE " +
            "  WHEN s.scheduleDate = :today THEN 1 " +
            "  WHEN s.scheduleDate > :today THEN 2 " +
            "  ELSE 3 " +
            "END ASC, " +
            "s.scheduleDate ASC, " +
            "s.startTime ASC")
    Page<DoctorSchedule> findAllSchedules(
            @Param("today") LocalDate today,
            @Param("now") LocalTime now,
            @Param("doctorName") String doctorName,
            @Param("specialization") String specialization,
            @Param("searchDate") LocalDate searchDate,
            Pageable pageable
    );

    @Query("SELECT s FROM DoctorSchedule s " +
            "JOIN s.doctor d " +
            "JOIN d.specialization sp " +
            "WHERE s.isActive = true " +

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
            Pageable pageable
    );

    // Find all active schedules on or after a certain date (for finding upcoming)
    List<DoctorSchedule> findByScheduleDateAfterAndIsActiveTrue(LocalDate date);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM DoctorSchedule s " +
            "WHERE s.doctor.id = :doctorId " +
            "AND s.scheduleDate = :scheduleDate " +
            "AND s.isActive = true " +
            "AND s.startTime < :endTime " + // New start time is before existing end time
            "AND s.endTime > :startTime")  // New end time is after existing start time
    boolean existsOverlappingSchedule(
            @Param("doctorId") Long doctorId,
            @Param("scheduleDate") LocalDate scheduleDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /**
     * NEW METHOD FOR UPDATES:
     * Checks for overlapping schedules for a doctor on a specific date,
     * BUT excludes the schedule that is currently being updated.
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM DoctorSchedule s " +
            "WHERE s.doctor.id = :doctorId " +
            "AND s.scheduleDate = :scheduleDate " +
            "AND s.isActive = true " +
            "AND s.id != :scheduleIdToExclude " +  // <-- Exclude the current schedule
            "AND s.startTime < :endTime " +
            "AND s.endTime > :startTime")
    boolean existsOverlappingScheduleForUpdate(
            @Param("doctorId") Long doctorId,
            @Param("scheduleDate") LocalDate scheduleDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("scheduleIdToExclude") Long scheduleIdToExclude);
}
