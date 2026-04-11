DELIMITER $$

DROP PROCEDURE IF EXISTS GetScheduleFinancials$$

CREATE PROCEDURE GetScheduleFinancials(
    IN p_clinic_id INT,
    IN input_schedule_id INT
)
BEGIN
    SELECT
        -- 1. Appointment & Patient Details
        appt.id AS AppointmentID,
        appt.queuePosition AS QueueNo,
        CONCAT(p.firstName, ' ', p.lastName) AS PatientName,
        p.mobileNo AS PatientMobile,
        inv.id AS InvoiceID,

        -- Payment status
        inv_status.displayName AS PaymentStatus,
        inv_status.code AS PaymentStatusCode,
        inv_status.colorCode AS PaymentStatusColor,

        -- Appointment Status
        appt_status.displayName AS AppointmentStatus,
        appt_status.code AS AppointmentStatusCode,
        appt_status.colorCode AS AppointmentStatusColor,

        -- 2. Base Fees (Zeroed if cancelled)
        CASE WHEN appt_status.code = 'CANCELLED' THEN 0 ELSE ds.doctorFee END AS BaseDoctorFee,
        CASE WHEN appt_status.code = 'CANCELLED' THEN 0 ELSE ds.clinicFee END AS BaseClinicFee,

        -- 3. Doctor Side Calculations
        COALESCE(SUM(CASE WHEN it.typeName = 'DOCTOR_DISCOUNT' THEN ii.amount ELSE 0 END), 0) AS DoctorDiscountAmount,
        CASE
            WHEN appt_status.code = 'CANCELLED' THEN 0
            ELSE (ds.doctorFee + COALESCE(SUM(CASE WHEN it.typeName = 'DOCTOR_DISCOUNT' THEN ii.amount ELSE 0 END), 0))
        END AS NetDoctorPayout,

        -- 4. Clinic Side Calculations
        COALESCE(SUM(CASE WHEN it.typeName = 'CLINIC_DISCOUNT' THEN ii.amount ELSE 0 END), 0) AS ClinicDiscountAmount,
        CASE
            WHEN appt_status.code = 'CANCELLED' THEN 0
            ELSE (ds.clinicFee + COALESCE(SUM(CASE WHEN it.typeName = 'CLINIC_DISCOUNT' THEN ii.amount ELSE 0 END), 0))
        END AS NetClinicProfit,

        -- 5. Total Bill & Payment Tracking
        inv.subtotalAmount AS SubtotalAmount,
        inv.totalDiscount AS TotalDiscount,
        inv.finalAmount AS TotalPaidByPatient,
        inv.amountPaid AS AmountPaid,
        inv.balanceRemaining AS BalanceRemaining,
        inv.paymentDueDate AS PaymentDueDate,

        CASE
            WHEN inv.finalAmount > 0 THEN ROUND((inv.amountPaid / inv.finalAmount) * 100, 2)
            ELSE 0
        END AS PaymentPercentage,

        -- 6. Last Payment Details
        LastPayment.LastPaymentMethod,
        LastPayment.LastPaymentMethodCode,
        LastPayment.LastPaymentLocation,
        LastPayment.LastPaymentDate,
        COALESCE(LastPayment.TotalPayments, 0) AS TotalPayments

    FROM appointments appt

    -- Link Schedule & Patient
    JOIN doctorSchedule ds ON appt.doctor_schedule_id = ds.id AND ds.clinic_id = p_clinic_id
    JOIN patient p ON appt.patient_id = p.id AND p.clinic_id = p_clinic_id

    -- Link Invoice
    LEFT JOIN invoices inv ON appt.invoice_id = inv.id AND inv.clinic_id = p_clinic_id
    LEFT JOIN invoice_status_lookup inv_status ON inv.statusId = inv_status.id

    -- Link Appointment Status
    LEFT JOIN appointment_status_lookup appt_status ON appt.statusId = appt_status.id

    -- Link Invoice Items
    LEFT JOIN invoice_items ii ON inv.id = ii.invoice_id AND ii.clinic_id = p_clinic_id
    LEFT JOIN item_types it ON ii.item_type_id = it.id

    -- [ARCHITECT FIX 1]: Pre-Filter the Last Payment Window Function
    LEFT JOIN (
        SELECT
            pay.invoice_id,
            pm.displayName AS LastPaymentMethod,
            pm.code AS LastPaymentMethodCode,
            pl.displayName AS LastPaymentLocation,
            pay.paymentTimestamp AS LastPaymentDate,
            COUNT(*) OVER(PARTITION BY pay.invoice_id) AS TotalPayments,
            ROW_NUMBER() OVER(PARTITION BY pay.invoice_id ORDER BY pay.paymentTimestamp DESC) as rn
        FROM payments pay
        INNER JOIN payment_status_lookup ps ON pay.paymentStatusId = ps.id
        LEFT JOIN payment_method_lookup pm ON pay.paymentMethodId = pm.id
        LEFT JOIN payment_location_lookup pl ON pay.paymentLocationId = pl.id
        WHERE pay.clinic_id = p_clinic_id
          AND ps.code IN ('COMPLETED', 'REFUNDED')
          -- PRE-FILTER: Only look at payments for THIS schedule's invoices
          AND pay.invoice_id IN (
              SELECT invoice_id FROM appointments
              WHERE doctor_schedule_id = input_schedule_id AND clinic_id = p_clinic_id AND invoice_id IS NOT NULL
          )
    ) AS LastPayment ON inv.id = LastPayment.invoice_id AND LastPayment.rn = 1

    WHERE
        appt.doctor_schedule_id = input_schedule_id
        AND appt.clinic_id = p_clinic_id

    GROUP BY
        appt.id, appt.queuePosition, p.firstName, p.lastName, p.mobileNo, inv.id,
        inv_status.displayName, inv_status.code, inv_status.colorCode,
        appt_status.displayName, appt_status.code, appt_status.colorCode,
        ds.doctorFee, ds.clinicFee, inv.subtotalAmount, inv.totalDiscount, inv.finalAmount,
        inv.amountPaid, inv.balanceRemaining, inv.paymentDueDate,
        LastPayment.LastPaymentMethod, LastPayment.LastPaymentMethodCode,
        LastPayment.LastPaymentLocation, LastPayment.LastPaymentDate, LastPayment.TotalPayments
    ORDER BY
        appt.queuePosition ASC;

END$$

DELIMITER ;