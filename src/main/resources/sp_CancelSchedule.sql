DELIMITER //

CREATE PROCEDURE sp_CancelSchedule(
    IN p_clinic_id INT,
    IN p_schedule_id BIGINT,
    IN p_cancelled_by VARCHAR(255),
    OUT o_status_message VARCHAR(255),
    OUT o_is_success BOOLEAN
)
BEGIN
    DECLARE v_schedule_status_id INT;
    DECLARE v_cancelled_schedule_status_id INT;
    DECLARE v_cancelled_appointment_status_id INT;
    DECLARE v_cancelled_invoice_status_id INT;
    DECLARE v_booked_appt_status_id INT;
    DECLARE v_pending_payment_appt_status_id INT;

    -- Exit handler for SQL exceptions
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET o_is_success = FALSE;
        SET o_status_message = 'Failed to cancel schedule due to a database error.';
    END;

    -- Retrieve Lookup IDs
    SELECT id INTO v_cancelled_schedule_status_id FROM schedule_status_lookup WHERE code = 'CANCELLED';
    SELECT id INTO v_cancelled_appointment_status_id FROM appointment_status_lookup WHERE code = 'CANCELLED';
    SELECT id INTO v_cancelled_invoice_status_id FROM invoice_status_lookup WHERE code = 'CANCELLED';
    SELECT id INTO v_booked_appt_status_id FROM appointment_status_lookup WHERE code = 'BOOKED';
    SELECT id INTO v_pending_payment_appt_status_id FROM appointment_status_lookup WHERE code = 'PENDING_PAYMENT';

    -- Check if schedule exists and get current status
    SELECT statusId INTO v_schedule_status_id
    FROM doctorSchedule
    WHERE id = p_schedule_id AND clinic_id = p_clinic_id FOR UPDATE;

    IF v_schedule_status_id IS NULL THEN
        SET o_is_success = FALSE;
        SET o_status_message = 'Schedule not found.';
    ELSEIF v_schedule_status_id = v_cancelled_schedule_status_id THEN
        SET o_is_success = FALSE;
        SET o_status_message = 'Schedule is already cancelled.';
    ELSE
        START TRANSACTION;

        -- 1. Update Schedule Status
        UPDATE doctorSchedule
        SET statusId = v_cancelled_schedule_status_id,
            isBookingOpen = FALSE,
            isActive = FALSE,
            updatedBy = p_cancelled_by,
            updatedDate = NOW()
        WHERE id = p_schedule_id;

        -- 2. Update Invoices linked to active appointments for this schedule
        UPDATE invoices i
        INNER JOIN appointments a ON i.id = a.invoice_id
        SET i.statusId = v_cancelled_invoice_status_id,
            i.updatedBy = p_cancelled_by,
            i.updatedDate = NOW()
        WHERE a.doctor_schedule_id = p_schedule_id
          AND a.clinic_id = p_clinic_id
          AND a.statusId IN (v_booked_appt_status_id, v_pending_payment_appt_status_id);

        -- 3. Update Appointments Status
        UPDATE appointments
        SET statusId = v_cancelled_appointment_status_id,
            updatedBy = p_cancelled_by,
            updatedDate = NOW()
        WHERE doctor_schedule_id = p_schedule_id
          AND clinic_id = p_clinic_id
          AND statusId IN (v_booked_appt_status_id, v_pending_payment_appt_status_id);

        COMMIT;

        SET o_is_success = TRUE;
        SET o_status_message = 'Schedule and all active appointments cancelled successfully.';
    END IF;

END //

DELIMITER ;
