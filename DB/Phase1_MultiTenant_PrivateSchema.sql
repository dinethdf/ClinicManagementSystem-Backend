-- =================================================================================
-- ISOLATED CLINIC SCHEMA — FULL INITIALIZATION (COMPLETE SCHEMA)
-- Target DB: clinic_a115f66ae12942318e9c0bd5d0bc663a
-- Clinic ID: 6 | Subdomain: isolated-health
-- =================================================================================

CREATE DATABASE IF NOT EXISTS clinic_274fd4051cf240e288c19d8de19445e4;
USE clinic_274fd4051cf240e288c19d8de19445e4;

-- ─────────────────────────────────────────────────────────────────────────────────
-- 1. FUNDAMENTAL TABLES
-- ─────────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS clinics (
                                       id                   INT AUTO_INCREMENT PRIMARY KEY,
                                       clinic_code          VARCHAR(36)  NOT NULL UNIQUE,
    name                 VARCHAR(100) NOT NULL,
    subdomain            VARCHAR(50)  UNIQUE,
    email                VARCHAR(100),
    mobile_no_1          VARCHAR(20),
    mobile_no_2          VARCHAR(20),
    landline_no          VARCHAR(20),
    address              VARCHAR(255),
    city                 VARCHAR(50),
    district             VARCHAR(50),
    logo_url             VARCHAR(255),
    plan_code            VARCHAR(50)  NOT NULL DEFAULT 'PLAN_STANDARD',
    status               ENUM('TRIAL','ACTIVE','SUSPENDED','EXPIRED','CANCELLED') DEFAULT 'ACTIVE',
    registered_at        DATETIME,
    trial_ends_at        DATETIME,
    subscription_ends_at DATETIME,
    settings             JSON,
    clinic_secret        VARCHAR(20),
    is_deleted           TINYINT(1)   DEFAULT 0,
    encrypted_secrets    TEXT,
    created_at           DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME
    ) ENGINE=InnoDB;

INSERT INTO clinics (id, clinic_code, name, subdomain, plan_code, status, registered_at)
VALUES (6, 'a115f66a-e129-4231-8e9c-0bd5d0bc663a', 'Isolated Health Clinic', 'isolated-health', 'PLAN_STANDARD', 'ACTIVE', NOW());

CREATE TABLE IF NOT EXISTS user_role (
                                         id   INT         PRIMARY KEY,
                                         name VARCHAR(50) NOT NULL UNIQUE
    ) ENGINE=InnoDB;

INSERT INTO user_role (id, name) VALUES
                                     (1, 'ADMIN'), (2, 'CO-ADMIN'), (3, 'DOCTOR'), (4, 'PATIENT'),
                                     (5, 'STAFF'), (6, 'RECEPTION'), (7, 'NURSE'), (8, 'SUPER-ADMIN');

CREATE TABLE IF NOT EXISTS specialization (
                                              id   INT AUTO_INCREMENT PRIMARY KEY,
                                              name VARCHAR(50) NOT NULL,
    note VARCHAR(100)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS item_types (
                                          id          INT AUTO_INCREMENT PRIMARY KEY,
                                          typeName    VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NULL
    ) ENGINE=InnoDB;

INSERT INTO item_types (typeName, description) VALUES
                                                   ('APPOINTMENT', 'Doctor Consultation Fees'),
                                                   ('PHARMACY', 'Medicine and Prescriptions'),
                                                   ('LAB', 'Laboratory Test Charges');

CREATE TABLE IF NOT EXISTS expense_categories (
                                                  id          INT AUTO_INCREMENT PRIMARY KEY,
                                                  name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    isActive    TINYINT DEFAULT 1
    ) ENGINE=InnoDB;

-- ─────────────────────────────────────────────────────────────────────────────────
-- 2. AUTHENTICATION & CONFIG
-- ─────────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS systemConfig (
                                            id          INT AUTO_INCREMENT PRIMARY KEY,
                                            clinic_id   INT NOT NULL,
                                            configKey   VARCHAR(100) NOT NULL,
    configValue VARCHAR(255) NOT NULL,
    createdDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy   VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy   VARCHAR(50) DEFAULT NULL,
    CONSTRAINT uq_clinic_config UNIQUE (clinic_id, configKey),
    CONSTRAINT fk_config_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS userAuth (
                                        Id        INT AUTO_INCREMENT PRIMARY KEY,
                                        clinic_id INT NOT NULL,
                                        email     VARCHAR(100) NOT NULL,
    password  VARCHAR(255) NOT NULL,
    role      INT,
    isEnable  BOOLEAN DEFAULT TRUE,
    detailsId BIGINT,
    FOREIGN KEY (role) REFERENCES user_role(id),
    CONSTRAINT fk_userAuth_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    UNIQUE INDEX idx_email_per_clinic (clinic_id, email)
    ) ENGINE=InnoDB;

-- Default Admin: admin@isolated.com / password
INSERT INTO userAuth (clinic_id, email, password, role, isEnable)
VALUES (6, 'dineth@admin.com', '$2a$12$Z/h2MTBuOrefYxbKkQGhcOxc5in8vvq0JY0yyxUnCncw8uL7HQQNm', 1, TRUE);

CREATE TABLE IF NOT EXISTS patientAuth (
                                           id               INT AUTO_INCREMENT PRIMARY KEY,
                                           clinic_id        INT NOT NULL,
                                           email            VARCHAR(255) DEFAULT NULL,
    password         VARCHAR(255) DEFAULT NULL,
    isEnable         TINYINT NOT NULL DEFAULT 1,
    isTempCredential TINYINT NOT NULL DEFAULT 1,
    role             INT DEFAULT NULL,
    detailsId        INT DEFAULT NULL,
    CONSTRAINT fk_patientAuth_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    FOREIGN KEY (role) REFERENCES user_role(id) ON DELETE SET NULL,
    UNIQUE INDEX idx_patient_email_per_clinic (clinic_id, email)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS otpVerification (
                                               id          INT AUTO_INCREMENT PRIMARY KEY,
                                               clinic_id   INT NOT NULL,
                                               mobileNo    VARCHAR(40) NOT NULL,
    email       VARCHAR(100) DEFAULT NULL,
    otpCode     VARCHAR(10) NOT NULL,
    expiresAt   DATETIME NOT NULL,
    verified    TINYINT DEFAULT 0,
    dailySmsCount INT DEFAULT 0,
    lastSmsDate DATE DEFAULT NULL,
    createdDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy   VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    updatedDate DATETIME DEFAULT NULL,
    updatedBy   VARCHAR(50) DEFAULT NULL,
    CONSTRAINT fk_otp_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    UNIQUE KEY uk_mobile_clinic (clinic_id, mobileNo)
    ) ENGINE=InnoDB;

-- ─────────────────────────────────────────────────────────────────────────────────
-- 3. PATIENTS & DOCTORS
-- ─────────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS insurance (
                                         id            INT AUTO_INCREMENT PRIMARY KEY,
                                         clinic_id     INT NOT NULL,
                                         provider_name VARCHAR(100) NOT NULL,
    policy_number VARCHAR(100) NOT NULL,
    coverage_type VARCHAR(50),
    isActive      TINYINT NOT NULL DEFAULT 0,
    expiration_date DATE,
    CONSTRAINT fk_insurance_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS patient (
                                       id               INT AUTO_INCREMENT PRIMARY KEY,
                                       clinic_id        INT NOT NULL,
                                       patient_auth_id  INT DEFAULT NULL,
                                       patientNo        VARCHAR(100) NULL,
    firstName        VARCHAR(50) NOT NULL,
    lastName         VARCHAR(50) NOT NULL,
    fullName         VARCHAR(255),
    gender           TINYINT UNSIGNED NOT NULL,
    nic              VARCHAR(20),
    dob              DATE NOT NULL,
    address          TEXT DEFAULT NULL,
    city             VARCHAR(100),
    district         VARCHAR(100),
    email            VARCHAR(150) DEFAULT NULL,
    mobileNo         VARCHAR(20) DEFAULT NULL,
    bloodGroup       ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-') DEFAULT NULL,
    registrationDate DATETIME,
    profileImg       VARCHAR(255),
    isActive         TINYINT NOT NULL DEFAULT 1,
    isApproved       TINYINT NOT NULL DEFAULT 1,
    insurance_id     INT DEFAULT NULL,
    createdDate      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy        VARCHAR(50) NOT NULL,
    updatedDate      DATETIME DEFAULT NULL,
    updatedBy        VARCHAR(50),
    CONSTRAINT fk_patient_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_patient_auth FOREIGN KEY (patient_auth_id) REFERENCES patientAuth(id) ON DELETE SET NULL,
    CONSTRAINT fk_patient_insurance FOREIGN KEY (insurance_id) REFERENCES insurance(id) ON DELETE SET NULL,
    UNIQUE INDEX idx_patientNo_per_clinic (clinic_id, patientNo),
    UNIQUE INDEX idx_nic_per_clinic (clinic_id, nic)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS patient_allergies (
                                                 id          INT AUTO_INCREMENT PRIMARY KEY,
                                                 clinic_id   INT NOT NULL,
                                                 patient_id  INT NOT NULL,
                                                 allergyName VARCHAR(100) NOT NULL,
    notes       TEXT,
    CONSTRAINT fk_allergy_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS patient_chronic_diseases (
                                                        id          INT AUTO_INCREMENT PRIMARY KEY,
                                                        clinic_id   INT NOT NULL,
                                                        patient_id  INT NOT NULL,
                                                        diseaseName VARCHAR(100) NOT NULL,
    notes       TEXT,
    CONSTRAINT fk_disease_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS doctor (
                                      id              INT AUTO_INCREMENT PRIMARY KEY,
                                      clinic_id       INT NOT NULL,
                                      auth_id         INT DEFAULT NULL,
                                      firstName       VARCHAR(50) NOT NULL,
    lastName        VARCHAR(50) NOT NULL,
    gender          TINYINT UNSIGNED NOT NULL,
    nic             VARCHAR(20),
    email           VARCHAR(150),
    mobileNo        VARCHAR(20),
    slmcNumber      VARCHAR(50),
    specialization_id INT NOT NULL,
    profileImg      VARCHAR(255),
    isApproved      TINYINT NOT NULL DEFAULT 1,
    createdDate     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy       VARCHAR(50) NOT NULL,
    CONSTRAINT fk_doctor_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_doctor_specialization FOREIGN KEY (specialization_id) REFERENCES specialization(id),
    UNIQUE INDEX idx_slmc_per_clinic (clinic_id, slmcNumber),
    UNIQUE INDEX idx_doc_email_per_clinic (clinic_id, email)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS clinic_staff (
                                            id          INT AUTO_INCREMENT PRIMARY KEY,
                                            clinic_id   INT NOT NULL,
                                            auth_id     INT DEFAULT NULL,
                                            firstName   VARCHAR(50) NOT NULL,
    lastName    VARCHAR(50) NOT NULL,
    email       VARCHAR(150),
    mobileNo    VARCHAR(20),
    profileImg  VARCHAR(255),
    designation VARCHAR(100),
    isApproved  TINYINT NOT NULL DEFAULT 1,
    createdDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy   VARCHAR(50) NOT NULL,
    CONSTRAINT fk_staff_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id)
    ) ENGINE=InnoDB;

-- ─────────────────────────────────────────────────────────────────────────────────
-- 4. CLINIC OPERATIONS (SCHEDULES, APPOINTMENTS, BILLING)
-- ─────────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS doctorSchedule (
                                              id              INT AUTO_INCREMENT PRIMARY KEY,
                                              clinic_id       INT NOT NULL,
                                              doctor_id       INT NOT NULL,
                                              scheduleDate    DATE NOT NULL,
                                              startTime       TIME NOT NULL,
                                              endTime         TIME NOT NULL,
                                              doctorFee       DECIMAL(10,2) NOT NULL,
    clinicFee       DECIMAL(10,2) NOT NULL,
    maxPatientCount INT NOT NULL,
    registeredPatientCount INT NOT NULL DEFAULT 0,
    lastQueueNumber INT NOT NULL DEFAULT 0,
    isActive        TINYINT DEFAULT 1,
    status          ENUM('ACTIVE', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    bookingStatus   ENUM('OPEN', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    isApproved      TINYINT NOT NULL DEFAULT 1,
    createdDate     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy       VARCHAR(50) NOT NULL,
    CONSTRAINT fk_schedule_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    FOREIGN KEY (doctor_id) REFERENCES doctor(id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS invoices (
                                        id              INT AUTO_INCREMENT PRIMARY KEY,
                                        clinic_id       INT NOT NULL,
                                        patient_id      INT NOT NULL,
                                        status          ENUM('UNPAID', 'PAID', 'PARTIAL', 'CANCELLED') NOT NULL DEFAULT 'UNPAID',
    subtotalAmount  DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    totalDiscount   DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    finalAmount     DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    dueDate         DATE NULL,
    createdDate     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy       VARCHAR(50) NOT NULL,
    CONSTRAINT fk_invoice_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_invoice_patient FOREIGN KEY (patient_id) REFERENCES patient(id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS invoice_items (
                                             id           INT AUTO_INCREMENT PRIMARY KEY,
                                             clinic_id    INT NOT NULL,
                                             invoice_id   INT NOT NULL,
                                             item_type_id INT NOT NULL,
                                             description  VARCHAR(255) NOT NULL,
    amount       DECIMAL(10, 2) NOT NULL,
    createdDate  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy    VARCHAR(50) NOT NULL,
    CONSTRAINT fk_inv_item_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_item_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_type FOREIGN KEY (item_type_id) REFERENCES item_types(id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS payments (
                                        id               INT AUTO_INCREMENT PRIMARY KEY,
                                        clinic_id        INT NOT NULL,
                                        invoice_id       INT NOT NULL,
                                        amountPaid       DECIMAL(10, 2) NOT NULL,
    paymentMethod    ENUM('CASH', 'CARD', 'BANK_TRANSFER', 'ONLINE', 'INSURANCE') NOT NULL,
    paymentStatus    ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    transactionId    VARCHAR(255) NULL,
    paymentTimestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdDate      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy        VARCHAR(50) NOT NULL,
    CONSTRAINT fk_payment_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_payment_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS appointments (
                                            id                 INT AUTO_INCREMENT PRIMARY KEY,
                                            clinic_id          INT NOT NULL,
                                            queuePosition      INT NOT NULL,
                                            note               TEXT NULL,
                                            status             ENUM('PENDING_PAYMENT', 'BOOKED', 'ONGOING', 'COMPLETED', 'CANCELLED', 'NOSHOW') NOT NULL DEFAULT 'PENDING_PAYMENT',
    doctor_schedule_id INT NOT NULL,
    patient_id         INT NOT NULL,
    invoice_id         INT NULL,
    createdDate        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy          VARCHAR(50) NOT NULL,
    CONSTRAINT fk_appt_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_appointment_schedule FOREIGN KEY (doctor_schedule_id) REFERENCES doctorSchedule(id),
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES patient(id),
    CONSTRAINT fk_appointment_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE SET NULL,
    UNIQUE INDEX idx_unique_queue (clinic_id, doctor_schedule_id, queuePosition)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS prescriptions (
                                             id             INT AUTO_INCREMENT PRIMARY KEY,
                                             clinic_id      INT NOT NULL,
                                             appointment_id INT NOT NULL,
                                             status         ENUM('ISSUED', 'DISPENSED', 'CANCELLED') DEFAULT 'ISSUED',
    note           TEXT,
    createdDate    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy      VARCHAR(50) NOT NULL,
    CONSTRAINT fk_pres_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_prescriptions_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    UNIQUE INDEX idx_appointment_unique (appointment_id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS prescription_items (
                                                  id              INT AUTO_INCREMENT PRIMARY KEY,
                                                  prescription_id INT NOT NULL,
                                                  medicineName    VARCHAR(255) NOT NULL,
    dosage          VARCHAR(100),
    quantity        VARCHAR(50),
    duration        VARCHAR(50),
    instructions    TEXT,
    CONSTRAINT fk_items_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

-- ─────────────────────────────────────────────────────────────────────────────────
-- 5. LAB & FINANCIALS
-- ─────────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS lab_test_types (
                                              id          INT AUTO_INCREMENT PRIMARY KEY,
                                              clinic_id   INT NOT NULL,
                                              name        VARCHAR(150) NOT NULL,
    code        VARCHAR(50),
    description TEXT,
    test_fee    DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    clinic_fee  DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    isActive    TINYINT DEFAULT 1,
    createdDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy   VARCHAR(50) NOT NULL,
    CONSTRAINT fk_lab_type_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT uk_lab_code_clinic UNIQUE (clinic_id, code)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS patient_lab_requests (
                                                    id               INT AUTO_INCREMENT PRIMARY KEY,
                                                    clinic_id        INT NOT NULL,
                                                    patient_id       INT NOT NULL,
                                                    invoice_id       INT NULL,
                                                    appointment_id   INT NULL,
                                                    lab_test_type_id INT NOT NULL,
                                                    status           ENUM('PENDING', 'SAMPLE_COLLECTED', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    payment_status   ENUM('PENDING', 'PAID', 'REFUND') DEFAULT 'PENDING',
    billed_amount    DECIMAL(10, 2) NOT NULL,
    createdDate      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy        VARCHAR(50) NOT NULL,
    CONSTRAINT fk_lab_req_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_lab_req_patient FOREIGN KEY (patient_id) REFERENCES patient(id),
    CONSTRAINT fk_lab_req_type FOREIGN KEY (lab_test_type_id) REFERENCES lab_test_types(id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS doctor_schedule_payments (
                                                        id                 INT AUTO_INCREMENT PRIMARY KEY,
                                                        clinic_id          INT NOT NULL,
                                                        doctor_schedule_id INT NOT NULL,
                                                        totalDoctorFee     DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    finalPayableAmount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status             ENUM('PENDING', 'PARTIAL', 'PAID') NOT NULL DEFAULT 'PENDING',
    createdDate        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy          VARCHAR(50) NOT NULL,
    CONSTRAINT fk_dsp_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_dsp_schedule FOREIGN KEY (doctor_schedule_id) REFERENCES doctorSchedule(id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS operational_expenses (
                                                    id                  INT AUTO_INCREMENT PRIMARY KEY,
                                                    clinic_id           INT NOT NULL,
                                                    expense_category_id INT NOT NULL,
                                                    description         VARCHAR(255) NOT NULL,
    amount              DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    dueDate             DATE NULL,
    status              ENUM('PENDING', 'PARTIAL', 'PAID') NOT NULL DEFAULT 'PENDING',
    createdDate         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy           VARCHAR(50) NOT NULL,
    CONSTRAINT fk_opex_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_expense_category FOREIGN KEY (expense_category_id) REFERENCES expense_categories(id)
    ) ENGINE=InnoDB;

-- =================================================================================
-- DONE
-- =================================================================================
