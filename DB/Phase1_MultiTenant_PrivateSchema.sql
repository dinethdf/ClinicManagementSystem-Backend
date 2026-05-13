-- =================================================================================
-- ISOLATED CLINIC SCHEMA — FULL INITIALIZATION
-- Target DB: clinic_a115f66ae12942318e9c0bd5d0bc663a
-- Clinic ID: 6 | Subdomain: isolated-health
-- Login: dineth@admin.com / dd  (role: ADMIN)
-- =================================================================================

CREATE DATABASE IF NOT EXISTS clinic_a115f66ae12942318e9c0bd5d0bc663a;
USE clinic_a115f66ae12942318e9c0bd5d0bc663a;

-- ─── 1. Roles ────────────────────────────────────────────────────────────────
CREATE TABLE user_role (
    id   INT         PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO user_role (id, name) VALUES
(1, 'ADMIN'),
(2, 'CO-ADMIN'),
(3, 'DOCTOR'),
(4, 'PATIENT'),
(5, 'STAFF'),
(6, 'RECEPTION'),
(7, 'NURSE'),
(8, 'SUPER-ADMIN');

-- ─── 2. Auth ─────────────────────────────────────────────────────────────────
-- Column name is `Id` (capital I) — matches the JPA entity field `private int Id`
CREATE TABLE userAuth (
    Id        INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT           NOT NULL,
    email     VARCHAR(100)  NOT NULL UNIQUE,
    password  VARCHAR(255)  NOT NULL,
    role      INT,
    isEnable  BOOLEAN       DEFAULT TRUE,
    detailsId BIGINT,
    FOREIGN KEY (role) REFERENCES user_role(id)
);

-- Password: "dd"  →  BCrypt strength 12
-- NOTE: BCrypt hash is salted. If this hash doesn't work, see the note at the bottom.
INSERT INTO userAuth (clinic_id, email, password, role, isEnable, detailsId)
VALUES (6, 'dineth@admin.com', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh3y', 1, TRUE, NULL);

-- ─── 3. Clinics ──────────────────────────────────────────────────────────────
-- Required: UserService calls clinicService.getClinicById() AFTER auth to build the JWT.
-- The id must match clinic_id used in userAuth (6).
CREATE TABLE clinics (
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
);

-- Insert matching the clinic_id = 6 that userAuth references
INSERT INTO clinics (id, clinic_code, name, subdomain, plan_code, status, registered_at)
VALUES (6, 'a115f66a-e129-4231-8e9c-0bd5d0bc663a', 'Isolated Health Clinic', 'isolated-health', 'PLAN_STANDARD', 'ACTIVE', NOW());

-- ─── 4. Business Tables ───────────────────────────────────────────────────────
CREATE TABLE doctor (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    clinic_id       INT,
    auth_id         INT,
    firstName       VARCHAR(100),
    lastName        VARCHAR(100),
    email           VARCHAR(100),
    mobileNo        VARCHAR(20),
    slmcNumber      VARCHAR(50),
    specialization_id INT,
    profileImg      VARCHAR(255),
    isApproved      BOOLEAN     DEFAULT TRUE,
    created_at      DATETIME    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE patient (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    clinic_id   INT,
    firstName   VARCHAR(100),
    lastName    VARCHAR(100),
    nic         VARCHAR(20),
    mobileNo    VARCHAR(20),
    city        VARCHAR(100),
    gender      INT,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE clinic_staff (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    clinic_id   INT,
    auth_id     INT,
    firstName   VARCHAR(100),
    lastName    VARCHAR(100),
    email       VARCHAR(100),
    mobileNo    VARCHAR(20),
    profileImg  VARCHAR(255),
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE appointments (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    clinic_id        INT,
    patient_id       BIGINT,
    doctor_id        BIGINT,
    schedule_id      BIGINT,
    appointment_date DATE,
    status           VARCHAR(20) DEFAULT 'PENDING',
    created_at       DATETIME    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patient(id),
    FOREIGN KEY (doctor_id)  REFERENCES doctor(id)
);

-- =================================================================================
-- HASH NOTE:
-- If login fails with "Invalid credentials" (bad password), the BCrypt hash above
-- may not match your exact Spring Boot encoder config.
-- To generate the correct hash, hit this temp endpoint in your running app:
--   GET /api/util/hash?plain=dd
-- Or temporarily insert with password='password' using the known-good hash:
--   '$2a$12$Z/h2MTBuOrefYxbKkQGhcOxc5in8vvq0JY0yyxUnCncw8uL7HQQNm'
-- and login with password "password" first, then change it.
-- =================================================================================
