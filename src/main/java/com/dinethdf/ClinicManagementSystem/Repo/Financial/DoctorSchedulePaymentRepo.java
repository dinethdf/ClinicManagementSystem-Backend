package com.dinethdf.ClinicManagementSystem.Repo.Financial;

import com.dinethdf.ClinicManagementSystem.DTO.Financial.PayoutView;
import com.dinethdf.ClinicManagementSystem.DTO.Financial.PendingBillView;
import com.dinethdf.ClinicManagementSystem.Model.Financial.DoctorSchedulePayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorSchedulePaymentRepo extends JpaRepository<DoctorSchedulePayment, Integer> {

    boolean existsByDoctorScheduleIdAndClinicId(Integer doctorScheduleId, Integer clinicId);

    Optional<DoctorSchedulePayment> findByDoctorScheduleIdAndClinicId(Integer doctorScheduleId, Integer clinicId);

    List<DoctorSchedulePayment> findByStatusAndClinicId(DoctorSchedulePayment.PayoutStatus status, Integer clinicId);

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
        WHERE p.clinicId = :clinicId
        AND p.status = :status
        AND (:doctorId IS NULL OR d.id = :doctorId)
    """)
    List<PendingBillView> findPendingBillsFiltered(
            @Param("status") DoctorSchedulePayment.PayoutStatus status,
            @Param("doctorId") Integer doctorId,
            @Param("clinicId") Integer clinicId
    );

    @Query("""
        SELECT
            p.id AS payoutId,
            ds.id AS scheduleId,
            CONCAT(d.firstName, ' ', d.lastName) AS doctorName,
            ds.scheduleDate AS scheduleDate,
            p.totalDoctorFee AS totalFee,
            p.finalPayableAmount AS finalPayableAmount,
            p.status AS status,
            p.createdBy AS createdBy
        FROM DoctorSchedulePayment p
        JOIN p.doctorSchedule ds
        JOIN ds.doctor d
        WHERE
            p.clinicId = :clinicId
            AND (:doctorId IS NULL OR d.id = :doctorId)
            AND (:status IS NULL OR p.status = :status)
            AND (:startDate IS NULL OR ds.scheduleDate >= :startDate)
            AND (:endDate IS NULL OR ds.scheduleDate <= :endDate)
    """)
    Page<PayoutView> searchPayouts(
            @Param("doctorId") Long doctorId,
            @Param("status") DoctorSchedulePayment.PayoutStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("clinicId") Integer clinicId,
            Pageable pageable
    );

    // --- NEW: Sum with Date Range ---
    @Query("""
        SELECT COALESCE(SUM(p.finalPayableAmount), 0)
        FROM DoctorSchedulePayment p
        JOIN p.doctorSchedule ds
        WHERE p.clinicId = :clinicId
        AND p.status = :status
        AND (:startDate IS NULL OR ds.scheduleDate >= :startDate)
        AND (:endDate IS NULL OR ds.scheduleDate <= :endDate)
    """)
    BigDecimal sumByStatusAndDate(
            @Param("status") DoctorSchedulePayment.PayoutStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("clinicId") Integer clinicId
    );
}