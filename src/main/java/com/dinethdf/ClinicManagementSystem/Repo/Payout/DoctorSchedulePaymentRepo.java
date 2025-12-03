package com.dinethdf.ClinicManagementSystem.Repo.Payout;

import com.dinethdf.ClinicManagementSystem.Model.Financial.DoctorSchedulePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorSchedulePaymentRepo extends JpaRepository<DoctorSchedulePayment, Integer> {

    // Check if bill exists
    boolean existsByDoctorScheduleId(Integer doctorScheduleId);

    // Find bill by Schedule ID
    Optional<DoctorSchedulePayment> findByDoctorScheduleId(Integer doctorScheduleId);

    // Get list of unpaid bills for the Admin Table
    List<DoctorSchedulePayment> findByStatus(DoctorSchedulePayment.PayoutStatus status);
}