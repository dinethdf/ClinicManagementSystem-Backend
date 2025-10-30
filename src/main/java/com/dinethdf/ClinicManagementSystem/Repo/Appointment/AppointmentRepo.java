package com.dinethdf.ClinicManagementSystem.Repo.Appointment;

import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;


import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public interface AppointmentRepo extends JpaRepository<Appointment, Long> {

    @Procedure(name = "Appointment.spBookAppointment")
    Map<String, Object> sp_BookAppointment(
            @Param("p_createdBy") String createdBy,
            @Param("p_doctor_schedule_id") Long doctorScheduleId,
            @Param("p_patient_id") Long patientId,
            @Param("p_note") String note
    );

    Optional<Appointment> findByAppointmentNumber(String appointmentNumber);
}