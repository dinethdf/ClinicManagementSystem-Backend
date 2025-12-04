package com.dinethdf.ClinicManagementSystem.Repo.Payout;

import com.dinethdf.ClinicManagementSystem.DTO.Financial.PendingBillView;
import com.dinethdf.ClinicManagementSystem.Model.Financial.DoctorSchedulePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
        SELECT
            p.id AS payoutId,
            ds.id AS scheduleId,
            CONCAT(d.firstName, ' ', d.lastName) AS doctorName,
            ds.scheduleDate AS scheduleDate,
            p.totalDoctorFee AS totalFee,
            p.totalDoctorDiscounts AS totalDoctorDiscounts,
            p.finalPayableAmount AS finalPayableAmount,
            p.status AS status
        FROM DoctorSchedulePayment p
        JOIN p.doctorSchedule ds
        JOIN ds.doctor d
        WHERE p.status = :status
        AND (:doctorId IS NULL OR d.id = :doctorId)
    """)
    List<PendingBillView> findPendingBillsFiltered(
            @Param("status") DoctorSchedulePayment.PayoutStatus status,
            @Param("doctorId") Integer doctorId
    );
}