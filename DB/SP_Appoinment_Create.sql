DELIMITER $$
DROP PROCEDURE IF EXISTS sp_BookAppointment$$

CREATE PROCEDURE sp_BookAppointment(
    IN p_createdBy VARCHAR(50),
    IN p_doctor_schedule_id INT,
    IN p_patient_id INT,
    IN p_note TEXT,

    OUT o_queuePosition INT,
    OUT o_appointmentNumber VARCHAR(50),
    OUT o_newInvoiceId INT,
    OUT o_statusMessage VARCHAR(50)
)
BEGIN
    -- DECLARE VARIABLES
    DECLARE v_maxPatientCount INT;
    DECLARE v_currentBookedCount INT;
    DECLARE v_isScheduleActive TINYINT(1);
    DECLARE v_consultationFee DECIMAL(10, 2);
    DECLARE v_patientBookingCount INT;
    DECLARE v_newQueuePosition INT;
    DECLARE v_newAppointmentNumber VARCHAR(50);
    DECLARE v_newAppointmentId INT;
    DECLARE v_appointmentItemTypeId INT;
    DECLARE v_now DATETIME;

    -- ERROR HANDLING
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET o_statusMessage = 'ERROR_SQL_EXCEPTION';
    END;

    -- == 3. SET VARIABLES & START TRANSACTION ==
    SET v_now = NOW();

    START TRANSACTION;
    -- == 4. LOCK SCHEDULE & GET CURRENT DATA ==
    SELECT
        maxPatientCount,
        registeredPatientCount,
        isActive,
        payment
    INTO
        v_maxPatientCount,
        v_currentBookedCount,
        v_isScheduleActive,
        v_consultationFee
    FROM
        doctorSchedule
    WHERE
        id = p_doctor_schedule_id
    FOR UPDATE;

    -- == 5. VALIDATE BUSINESS RULES ==
    IF v_maxPatientCount IS NULL THEN
        SET o_statusMessage = 'ERROR_INVALID_SCHEDULE';
        ROLLBACK;
    ELSEIF v_isScheduleActive = 0 THEN
        SET o_statusMessage = 'ERROR_SCHEDULE_INACTIVE';
        ROLLBACK;
    ELSEIF v_currentBookedCount >= v_maxPatientCount THEN
        SET o_statusMessage = 'ERROR_SCHEDULE_FULL';
        ROLLBACK;
    ELSE
        -- == 6. CHECK FOR DUPLICATE BOOKING ==
        SELECT COUNT(*)
        INTO v_patientBookingCount
        FROM appointments
        WHERE doctor_schedule_id = p_doctor_schedule_id
          AND patient_id = p_patient_id
          AND status = 'Booked';

        IF v_patientBookingCount > 0 THEN
            SET o_statusMessage = 'ERROR_DUPLICATE_PATIENT';
            ROLLBACK;
        ELSE
            -- == 7. ALL CHECKS PASSED - CREATE BOOKING ==
            SET v_newQueuePosition = v_currentBookedCount + 1;

            SET v_newAppointmentNumber = CONCAT(
                'AP-',
                LPAD(CONV(p_doctor_schedule_id, 10, 36), 4, '0'),
                '-',
                v_newQueuePosition
            );

            -- a. Insert the new appointment
            INSERT INTO appointments (
                appointmentNumber,
                doctor_schedule_id,
                patient_id,
                queuePosition,
                note,
                status,
                createdDate,
                createdBy
            ) VALUES (
                v_newAppointmentNumber,
                p_doctor_schedule_id,
                p_patient_id,
                v_newQueuePosition,
                p_note,
                'PENDING_PAYMENT',
                v_now,
                p_createdBy
            );

            SET v_newAppointmentId = LAST_INSERT_ID();

            -- b. Update the schedule's patient count
            UPDATE doctorSchedule
            SET registeredPatientCount = v_newQueuePosition
            WHERE id = p_doctor_schedule_id;

            -- == 8. AUTOMATIC INVOICE CREATION ==

            -- c. Find the 'APPOINTMENT' item type ID
            SELECT id INTO v_appointmentItemTypeId
            FROM item_types
            WHERE typeName = 'APPOINTMENT' LIMIT 1;

            -- d. Create the master invoice
            INSERT INTO invoices (
                patient_id,
                status,
                totalAmount,
                createdDate,
                createdBy
            ) VALUES (
                p_patient_id,
                'UNPAID',
                v_consultationFee,
                v_now,
                p_createdBy
            );

            SET o_newInvoiceId = LAST_INSERT_ID();

            -- e. Create the invoice line item
            INSERT INTO invoice_items (
                invoice_id,
                item_type_id,
                item_id,
                description,
                amount,
                createdDate,
                createdBy
            ) VALUES (
                o_newInvoiceId,
                v_appointmentItemTypeId,
                v_newAppointmentId,
                'Doctor Consultation',
                v_consultationFee,
                v_now,
                p_createdBy
            );

            -- == 9. SUCCESS! ==
            SET o_queuePosition = v_newQueuePosition;
            SET o_appointmentNumber = v_newAppointmentNumber;
            SET o_statusMessage = 'BOOKED_SUCCESS';

            COMMIT;

        END IF;
    END IF;

END$$

-- 3. Reset the delimiter back to ;
DELIMITER ;