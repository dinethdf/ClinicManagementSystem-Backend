package com.dinethdf.ClinicManagementSystem.Repo.DoctorSchedule;

import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface DoctorScheduleRepo extends JpaRepository<DoctorSchedule, Long> {

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
            "    (s.scheduleDate = :today AND s.endTime > :now) " +  // Condition for today
            "    OR " +
            "    (s.scheduleDate > :today) " +                     // Condition for future dates
            ")")
    List<DoctorSchedule> findUpcomingSchedules(
            @Param("doctorId") Long doctorId,
            @Param("today") LocalDate today,
            @Param("now") LocalTime now
    );

    // Find all active schedules on or after a certain date (for finding upcoming)
    List<DoctorSchedule> findByScheduleDateAfterAndIsActiveTrue(LocalDate date);

//    // Check if a conflicting schedule already exists
//    boolean existsByDoctorIdAndScheduleDateAndScheduleTime(Long doctorId, LocalDate date, LocalTime time);

    /**
     * Replaces the old existsByDoctorIdAndScheduleDateAndScheduleTime
     * * This checks for time range overlaps: (NewStartTime < ExistingEndTime) AND (NewEndTime > ExistingStartTime)
     * It will find any schedule that conflicts with the new proposed time.
     */
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
