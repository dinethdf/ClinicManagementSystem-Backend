-- ==================================================================================
-- PHASE 1 — MANUAL SQL STEPS
-- Run this script in MySQL Workbench or your SQL client
-- ==================================================================================

-- ─────────────────────────────────────────────────────────────────────────────────
-- STEP 1: Add tenant_mode column to existing clinicmsystem.clinics
--         (Needed so SHARED mode clinics carry their mode in the existing DB too)
-- ─────────────────────────────────────────────────────────────────────────────────
USE clinicmsystem;

ALTER TABLE clinics
    ADD COLUMN tenant_mode ENUM('SHARED', 'SCHEMA') NOT NULL DEFAULT 'SHARED'
    AFTER encrypted_secrets;

-- Verify (all existing rows should show SHARED):
-- SELECT id, clinic_code, tenant_mode FROM clinics;


-- ─────────────────────────────────────────────────────────────────────────────────
-- STEP 2: Create clinicmsystem_master database
--         (The tenant routing registry — only one table)
-- ─────────────────────────────────────────────────────────────────────────────────
CREATE DATABASE IF NOT EXISTS clinicmsystem_master
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE clinicmsystem_master;

CREATE TABLE IF NOT EXISTS clinics (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    clinic_code  VARCHAR(36)  NOT NULL,
    name         VARCHAR(100) NOT NULL,
    subdomain    VARCHAR(50)  NOT NULL,
    schema_name  VARCHAR(100) NOT NULL,          -- "shared" or "clinic_abc123uuid"
    tenant_mode  ENUM('SHARED','SCHEMA') NOT NULL DEFAULT 'SHARED',
    status       ENUM('TRIAL','ACTIVE','SUSPENDED','EXPIRED','CANCELLED') DEFAULT 'TRIAL',
    plan_code    VARCHAR(50)  NOT NULL DEFAULT 'PLAN_BASIC',
    is_deleted   BOOLEAN DEFAULT FALSE,
    
    -- Audit fields matching AuditWrapper convention
    createdDate  DATETIME     NOT NULL,
    createdBy    VARCHAR(100) NOT NULL DEFAULT 'System',
    updatedDate  DATETIME,
    updatedBy    VARCHAR(100),
    isApproved   BOOLEAN      NOT NULL DEFAULT FALSE,
    approvedDate DATETIME,
    approvedBy   VARCHAR(100),

    UNIQUE KEY  uk_clinic_code (clinic_code),
    UNIQUE KEY  uk_subdomain   (subdomain),
    INDEX       idx_status     (status, is_deleted)
) ENGINE=InnoDB;


-- ─────────────────────────────────────────────────────────────────────────────────
-- STEP 3: Mirror all existing SHARED mode clinics into the master registry
--         (So ClinicRoutingRegistry can find them at startup)
-- ─────────────────────────────────────────────────────────────────────────────────
INSERT INTO clinicmsystem_master.clinics
    (clinic_code, name, subdomain, schema_name, tenant_mode, status, plan_code, createdDate, createdBy)
SELECT
    clinic_code,
    name,
    subdomain,
    'shared'    AS schema_name,    -- SHARED mode → always route to clinicmsystem
    'SHARED'    AS tenant_mode,
    status,
    plan_code,
    created_at,                    -- Map from legacy created_at to createdDate
    'Migration'                    -- Set a default creator for mirrored rows
FROM clinicmsystem.clinics
WHERE is_deleted = FALSE;

-- Verify:
-- SELECT id, clinic_code, subdomain, schema_name, tenant_mode FROM clinicmsystem_master.clinics;


-- ─────────────────────────────────────────────────────────────────────────────────
-- DONE — Phase 1 SQL complete.
-- The application can now start. SHARED mode clinics work exactly as before.
-- Phase 2 will add the ClinicRoutingRegistry to load this data at startup.
-- ─────────────────────────────────────────────────────────────────────────────────
