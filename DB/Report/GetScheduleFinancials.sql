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

        -- Payment status (from lookup table only)
        inv_status.displayName AS PaymentStatus,
        inv_status.code AS PaymentStatusCode,
        inv_status.colorCode AS PaymentStatusColor,

        -- Appointment Status (from lookup table only)
        appt_status.displayName AS AppointmentStatus,
        appt_status.code AS AppointmentStatusCode,
        appt_status.colorCode AS AppointmentStatusColor,

        -- 2. Base Fees (From the Schedule settings)
        ds.doctorFee AS BaseDoctorFee,
        ds.clinicFee AS BaseClinicFee,

        -- 3. Doctor Side Calculations
        -- We sum up any item in this invoice that is a 'DOCTOR_DISCOUNT'
        COALESCE(SUM(CASE
            WHEN it.typeName = 'DOCTOR_DISCOUNT' THEN ii.amount
            ELSE 0
        END), 0) AS DoctorDiscountAmount,

        -- Net Doctor Payout = Fee + Discount (discount is negative)
        (ds.doctorFee + COALESCE(SUM(CASE
            WHEN it.typeName = 'DOCTOR_DISCOUNT' THEN ii.amount
            ELSE 0
        END), 0)) AS NetDoctorPayout,

        -- 4. Clinic Side Calculations
        -- We sum up any item in this invoice that is a 'CLINIC_DISCOUNT'
        COALESCE(SUM(CASE
            WHEN it.typeName = 'CLINIC_DISCOUNT' THEN ii.amount
            ELSE 0
        END), 0) AS ClinicDiscountAmount,

        -- Net Clinic Profit = Fee + Discount (discount is negative)
        (ds.clinicFee + COALESCE(SUM(CASE
            WHEN it.typeName = 'CLINIC_DISCOUNT' THEN ii.amount
            ELSE 0
        END), 0)) AS NetClinicProfit,

        -- 5. Total Bill & Payment Tracking (NEW FIELDS)
        inv.subtotalAmount AS SubtotalAmount,
        inv.totalDiscount AS TotalDiscount,
        inv.finalAmount AS TotalPaidByPatient,

        -- NEW: Payment tracking fields
        inv.amountPaid AS AmountPaid,
        inv.balanceRemaining AS BalanceRemaining,
        inv.paymentDueDate AS PaymentDueDate,

        -- NEW: Payment percentage
        CASE
            WHEN inv.finalAmount > 0 THEN
                ROUND((inv.amountPaid / inv.finalAmount) * 100, 2)
            ELSE 0
        END AS PaymentPercentage,

        -- Payment method summary (most recent payment - from lookup only)
        (SELECT pm.displayName
         FROM payments pay
         INNER JOIN payment_method_lookup pm ON pay.paymentMethodId = pm.id
         INNER JOIN payment_status_lookup ps ON pay.paymentStatusId = ps.id
         WHERE pay.invoice_id = inv.id
           AND pay.clinic_id = p_clinic_id
           AND ps.code = 'COMPLETED'
         ORDER BY pay.paymentTimestamp DESC
         LIMIT 1
        ) AS LastPaymentMethod,

        -- Payment method code
        (SELECT pm.code
         FROM payments pay
         INNER JOIN payment_method_lookup pm ON pay.paymentMethodId = pm.id
         INNER JOIN payment_status_lookup ps ON pay.paymentStatusId = ps.id
         WHERE pay.invoice_id = inv.id
           AND pay.clinic_id = p_clinic_id
           AND ps.code = 'COMPLETED'
         ORDER BY pay.paymentTimestamp DESC
         LIMIT 1
        ) AS LastPaymentMethodCode,

        -- Payment location (from lookup only)
        (SELECT pl.displayName
         FROM payments pay
         INNER JOIN payment_location_lookup pl ON pay.paymentLocationId = pl.id
         INNER JOIN payment_status_lookup ps ON pay.paymentStatusId = ps.id
         WHERE pay.invoice_id = inv.id
           AND pay.clinic_id = p_clinic_id
           AND ps.code = 'COMPLETED'
         ORDER BY pay.paymentTimestamp DESC
         LIMIT 1
        ) AS LastPaymentLocation,

        -- Last payment date
        (SELECT pay.paymentTimestamp
         FROM payments pay
         INNER JOIN payment_status_lookup ps ON pay.paymentStatusId = ps.id
         WHERE pay.invoice_id = inv.id
           AND pay.clinic_id = p_clinic_id
           AND ps.code = 'COMPLETED'
         ORDER BY pay.paymentTimestamp DESC
         LIMIT 1
        ) AS LastPaymentDate,

        -- Number of payments made
        (SELECT COUNT(*)
         FROM payments pay
         INNER JOIN payment_status_lookup ps ON pay.paymentStatusId = ps.id
         WHERE pay.invoice_id = inv.id
           AND pay.clinic_id = p_clinic_id
           AND ps.code = 'COMPLETED'
        ) AS TotalPayments

    FROM appointments appt

    -- Link to Schedule to get Base Fees
    JOIN doctorSchedule ds
        ON appt.doctor_schedule_id = ds.id
        AND ds.clinic_id = p_clinic_id

    -- Link to Patient for names
    JOIN patient p
        ON appt.patient_id = p.id
        AND p.clinic_id = p_clinic_id

    -- Link to Invoice
    LEFT JOIN invoices inv
        ON appt.invoice_id = inv.id
        AND inv.clinic_id = p_clinic_id

    -- NEW: Join invoice status lookup
    LEFT JOIN invoice_status_lookup inv_status
        ON inv.statusId = inv_status.id

    -- NEW: Join appointment status lookup
    LEFT JOIN appointment_status_lookup appt_status
        ON appt.statusId = appt_status.id

    -- Link to Items to find discounts
    LEFT JOIN invoice_items ii
        ON inv.id = ii.invoice_id
        AND ii.clinic_id = p_clinic_id

    LEFT JOIN item_types it
        ON ii.item_type_id = it.id

    WHERE
        appt.doctor_schedule_id = input_schedule_id
        AND appt.clinic_id = p_clinic_id
        -- Use lookup table code only (ENUM columns dropped)
        AND appt_status.code != 'CANCELLED'

    GROUP BY
        appt.id,
        p.id,
        inv.id,
        ds.doctorFee,
        ds.clinicFee,
        inv_status.displayName,
        inv_status.code,
        inv_status.colorCode,
        appt_status.displayName,
        appt_status.code

    ORDER BY
        appt.queuePosition ASC;

END$$

DELIMITER ;

-- ==================================================================================
-- USAGE EXAMPLES
-- ==================================================================================


-- Basic Usage:
CALL GetScheduleFinancials(1, 1);
/*
-- Expected Output Columns:
-- -------------------------
-- AppointmentID, QueueNo, PatientName, PatientMobile, InvoiceID

-- Payment Status (from invoice_status_lookup):
--   PaymentStatus: "Paid", "Partial", "Unpaid" (displayName)
--   PaymentStatusCode: "PAID", "PARTIAL", "UNPAID" (code)
--   PaymentStatusColor: "#28A745", "#FFA500", "#DC3545" (colorCode)

-- Appointment Status (from appointment_status_lookup):
--   AppointmentStatus: "Booked", "Completed", "Cancelled" (displayName)
--   AppointmentStatusCode: "BOOKED", "COMPLETED", "CANCELLED" (code)
--   AppointmentStatusColor: "#28A745", "#6C757D", "#DC3545" (colorCode)

-- Financial Breakdown:
--   BaseDoctorFee, BaseClinicFee
--   DoctorDiscountAmount, NetDoctorPayout
--   ClinicDiscountAmount, NetClinicProfit
--   SubtotalAmount, TotalDiscount, TotalPaidByPatient

-- Payment Tracking (NEW):
--   AmountPaid: Total paid so far (supports partial payments)
--   BalanceRemaining: Outstanding balance
--   PaymentDueDate: When payment is expected
--   PaymentPercentage: % of invoice paid (e.g., 50.00 for 50%)

-- Last Payment Details (from lookup tables):
--   LastPaymentMethod: "Cash", "Card", "Online" (from payment_method_lookup)
--   LastPaymentMethodCode: "CASH", "CARD", "ONLINE"
--   LastPaymentLocation: "Clinic Counter", "Online", "Mobile App" (from payment_location_lookup)
--   LastPaymentDate: Timestamp of most recent completed payment
--   TotalPayments: Count of all completed payments (for installment tracking)

-- NOTE: All status fields come from lookup tables only.
-- ENUM columns (status, paymentMethod, paymentStatus, paymentLocation) have been dropped.
*/