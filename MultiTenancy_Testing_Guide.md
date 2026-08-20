# CureGuard Hybrid Multi-Tenancy — Testing Guide

This document outlines the steps to verify the multi-tenancy implementation, covering database setup, legacy compatibility (SHARED mode), and isolated schema routing (SCHEMA mode).

## 1. Prerequisites (Database Setup)

Before starting the backend, you must prepare the master registry.

1.  **Run the Setup Script**: Execute `DB/Phase1_MultiTenant_Setup.sql` in your MySQL client.
    *   This creates `clinicmsystem_master`.
    *   It mirrors your existing clinics into the master registry as `SHARED` mode.
2.  **Create a Test Isolated Schema**:
    *   Create a new empty database: `CREATE DATABASE clinic_test;`
    *   Run your existing table scripts (patients, appointments, etc.) inside `clinic_test`.

---

## 2. Postman Testing Scenarios

### Scenario 1: Legacy SHARED Mode Login
**Goal**: Verify existing clinics still work normally.

*   **URL**: `POST {{baseUrl}}/signIn`
*   **Body (JSON)**:
    ```json
    {
        "username": "your_existing_admin",
        "password": "your_password",
        "loginType": "USER",
        "clinicId": 1
    }
    ```
*   **Expected Response**: A JWT token.
*   **Verification**: Call `GET {{baseUrl}}/api/clinic/info/details` with the Bearer token. It should return your clinic details from the `clinicmsystem` database.

---

### Scenario 2: Register a New Clinic (Public)
**Goal**: Verify that new clinics are dynamically added to the master registry.

*   **URL**: `POST {{baseUrl}}/api/public/registration/register-clinic`
*   **Body (JSON)**:
    ```json
    {
        "clinicName": "City Hospital",
        "subdomain": "city-hospital",
        "mobileNo1": "0112345678",
        "address": "123 Main St",
        "city": "Colombo"
    }
    ```
*   **Expected Response**: `200 OK` with a `clinicResponseDTO`.
*   **Verification**: Check the `clinicmsystem_master.clinics` table. A new row should appear with `tenant_mode = 'SHARED'` (default for public registration).

---

### Scenario 3: Isolated SCHEMA Mode Routing
**Goal**: Verify that a clinic configured for `SCHEMA` mode hits its dedicated database.

1.  **Prepare the Master Entry**:
    In `clinicmsystem_master.clinics`, update a clinic row manually:
    ```sql
    UPDATE clinics 
    SET tenant_mode = 'SCHEMA', 
        schema_name = 'clinic_test' 
    WHERE subdomain = 'city-hospital';
    ```
2.  **Login**: Use the credentials for this clinic.
3.  **Perform an Action**: Create a patient.
    *   **URL**: `POST {{baseUrl}}/api/patient/add`
    *   **Body**: (Standard patient JSON)
4.  **Verification**: 
    *   Check the `patients` table in `clinicmsystem` (shared) -> It should be EMPTY.
    *   Check the `patients` table in `clinic_test` (isolated) -> The patient should be THERE.

---

### Scenario 4: Public Endpoint Testing (X-Tenant-Code)
**Goal**: Verify that public endpoints (without JWT) correctly resolve the tenant via headers.

*   **URL**: `POST {{baseUrl}}/api/public/check-patient?query=0771234567&clinicId=1`
*   **Headers**: 
    *   `X-Tenant-Code`: `[The clinic_code for clinic ID 1]`
*   **Verification**: The system should use the registry to resolve the schema based on the header, even if `clinicId` is passed in the query param (the header resolution is more robust in our new filter).

---

## 3. Troubleshooting

| Issue | Potential Cause |
| :--- | :--- |
| **"Master registry update failed"** | Ensure the database user in `application.properties` has permissions on `clinicmsystem_master`. |
| **"Unknown Database: clinic_xyz"** | You set `schema_name` in the master table but didn't create the physical database in MySQL. |
| **Cache bleeding** | If Clinic A sees Clinic B's data, check if `CacheConfig.java` is using the `tenantKeyGenerator`. |
| **Token invalid** | Ensure the JWT contains the `clinicCode` claim. |
