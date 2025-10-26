package com.dinethdf.ClinicManagementSystem.Repo.DoctorSchedule;

import com.dinethdf.ClinicManagementSystem.Model.Appoinment.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
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
    List<DoctorSchedule> findByDoctorIdAndScheduleDateAfterAndIsActiveTrue(Long doctorId, LocalDate date);

    // Find all active schedules on or after a certain date (for finding upcoming)
    List<DoctorSchedule> findByScheduleDateAfterAndIsActiveTrue(LocalDate date);

    // Check if a conflicting schedule already exists
    boolean existsByDoctorIdAndScheduleDateAndScheduleTime(Long doctorId, LocalDate date, LocalTime time);
}
