package com.dinethdf.ClinicManagementSystem.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-local store for the current tenant's identity.
 *
 * Holds three values per request thread:
 *   currentTenantId   — Integer clinicId  (used by all @Query methods — unchanged)
 *   currentTenantCode — String clinicCode (UUID — used for routing registry lookups)
 *   currentSchema     — String schemaName ("shared" OR "clinic_abc123uuid")
 *                       Read by TenantRoutingDataSource to select the correct datasource.
 *
 * Backward compatibility: all existing calls to getCurrentTenant() / setCurrentTenant()
 * continue to work exactly as before. Zero changes required in services/repositories.
 */
public class TenantContext {

    private static final Logger logger = LoggerFactory.getLogger(TenantContext.class);

    // ── Existing (unchanged) ─────────────────────────────────────────────────────
    private static final ThreadLocal<Integer> currentTenantId = new ThreadLocal<>();

    // ── New ──────────────────────────────────────────────────────────────────────
    private static final ThreadLocal<String> currentTenantCode = new ThreadLocal<>();
    private static final ThreadLocal<String> currentSchema     = new ThreadLocal<>();

    // ── Existing API (backward compatible — zero changes needed in other classes) ─

    public static void setCurrentTenant(Integer tenantId) {
        logger.debug("Setting tenantId to {}", tenantId);
        currentTenantId.set(tenantId);
    }

    public static Integer getCurrentTenant() {
        return currentTenantId.get();
    }

    // ── New API ──────────────────────────────────────────────────────────────────

    /**
     * Sets all three tenant fields at once from a ClinicRoutingInfo.
     * Called by TenantResolutionFilter and JwtFilter.
     */
    public static void setFromRoutingInfo(Integer clinicId, String clinicCode, String schemaName) {
        logger.debug("Setting tenant context: id={}, code={}, schema={}", clinicId, clinicCode, schemaName);
        currentTenantId.set(clinicId);
        currentTenantCode.set(clinicCode);
        currentSchema.set(schemaName);
    }

    /**
     * Returns the schema name for this request.
     * "shared"             → route to clinicmsystem (existing shared DB)
     * "clinic_abc123uuid"  → route to that clinic's dedicated database
     *
     * Returns "shared" as a safe default if not set.
     */
    public static String getCurrentSchema() {
        String schema = currentSchema.get();
        return (schema != null) ? schema : "shared";
    }

    public static String getCurrentTenantCode() {
        return currentTenantCode.get();
    }

    /**
     * Clears ALL three ThreadLocals. Must be called in a finally block
     * after every request (done in JwtFilter and TenantResolutionFilter).
     */
    public static void clear() {
        logger.debug("Clearing tenant context");
        currentTenantId.remove();
        currentTenantCode.remove();
        currentSchema.remove();
    }
}