package com.dinethdf.ClinicManagementSystem.Repo.Financial;

import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import com.dinethdf.ClinicManagementSystem.Repo.Projection.ScheduleFinancialProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialRepo extends JpaRepository<Appointment, Long> {

    // Using CALL syntax for Stored Procedure
    @Query(value = "CALL GetScheduleFinancials(:scheduleId)", nativeQuery = true)
    List<ScheduleFinancialProjection> getScheduleFinancials(@Param("scheduleId") Integer scheduleId);
}