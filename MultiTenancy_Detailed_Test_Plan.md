# Multi-Tenancy Detailed Test Plan & Scenarios

This document provides advanced scenarios to verify the robustness of the hybrid multi-tenancy architecture. Use these scenarios to ensure security, data isolation, and performance.

---

## Scenario Group A: Security & Data Isolation
**Objective**: Ensure that one clinic can NEVER see or modify another clinic's data.

### 1. The "Token Theft" Test
*   **Step 1**: Log in to **Clinic A** and get `TOKEN_A`.
*   **Step 2**: Attempt to access a specific resource from **Clinic B** (e.g., `GET /api/patient/details/55`) using `TOKEN_A`.
*   **Expected Result**:
    *   If Clinic B's database is searched, it should return `404 Not Found` or `403 Forbidden` because the query will be routed to **Clinic A's database** (based on the token), and ID 55 doesn't exist there.
    *   **Crucial**: Under no circumstances should Clinic B's data be returned.

### 2. The "Header Injection" Test
*   **Step 1**: Log in to **Clinic A** and get `TOKEN_A`.
*   **Step 2**: Call an authenticated endpoint but add a different `X-Tenant-Code` header (pointing to Clinic B).
*   **Expected Result**: 
    *   The system must prioritize the **JWT Token** over the header. 
    *   Data should still come from **Clinic A**.

---

## Scenario Group B: Public Access (Anonymous)
**Objective**: Verify that public booking and patient checks work correctly for both database modes.

### 3. Public Booking — SHARED Clinic
*   **Action**: Call `/api/public/registration/register-clinic` with `X-Tenant-Code` of a Shared Clinic.
*   **Verification**: Ensure the record is saved in the `clinicmsystem` database.

### 4. Public Booking — SCHEMA Clinic
*   **Action**: Call the same endpoint but with the `X-Tenant-Code` of a Schema-isolated clinic.
*   **Verification**: Ensure the record is saved in the **isolated database** (e.g., `clinic_city_hosp`) and NOT in the shared database.

---

## Scenario Group C: Cache & Performance
**Objective**: Verify that the `tenantKeyGenerator` is working correctly.

### 5. Settings Isolation Test
*   **Step 1**: Update the currency to `USD` in **Clinic A**.
*   **Step 2**: Update the currency to `LKR` in **Clinic B**.
*   **Step 3**: Fetch settings for Clinic A.
*   **Expected Result**: Clinic A should get `USD`. If it gets `LKR`, your cache keys are overlapping (failing 5.1).

---

## Scenario Group D: Dynamic Lifecycle
**Objective**: Verify that the system handles new tenants and mode changes without restarts.

### 6. Zero-Downtime Registration
*   **Step 1**: Register a new clinic via the API while the server is running.
*   **Step 2**: Immediately attempt to log in or book an appointment for that clinic.
*   **Expected Result**: Success. (Proves `ClinicRoutingRegistry.registerNewClinic` is updating the in-memory cache correctly).

### 7. The "Promotion" (SHARED to SCHEMA)
*   **Step 1**: Start with a clinic in `SHARED` mode.
*   **Step 2**: Manually migrate its data to a new isolated database.
*   **Step 3**: Update the `tenant_mode` to `SCHEMA` and `schema_name` to the new DB in the master table.
*   **Step 4**: (Optional) Refresh registry or restart.
*   **Step 5**: Verify that all new traffic now goes to the isolated DB.

---

## Summary of Expected Responses

| Endpoint | Input Condition | Expected Status | Reason |
| :--- | :--- | :--- | :--- |
| `/api/patient/*` | Valid Token + Same Clinic | `200 OK` | Normal operation. |
| `/api/patient/*` | Valid Token + Wrong Clinic ID in Body | `404/403` | Data isolation ensures you only see your own DB. |
| `/api/public/*` | Missing `X-Tenant-Code` | `400 Bad Request` | System cannot resolve tenant context. |
| `/api/public/*` | Invalid `X-Tenant-Code` | `400/404` | Registry lookup fails. |
