package com.dinethdf.ClinicManagementSystem.config.multitenant;

/**
 * Controls how a clinic's data is stored.
 *
 * SHARED → clinic uses the shared clinicmsystem DB (table-level isolation via clinic_id).
 *          This is the default and how the system works today.
 *
 * SCHEMA → clinic has its own dedicated MySQL database: clinic_{clinicCode}.
 *          Full schema-level isolation. TenantRoutingDataSource routes
 *          all JPA calls to that clinic's private database.
 */
public enum TenantSchemaMode {
    SHARED,
    SCHEMA
}
