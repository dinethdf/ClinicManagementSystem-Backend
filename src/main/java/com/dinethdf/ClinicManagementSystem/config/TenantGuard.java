package com.dinethdf.ClinicManagementSystem.config;

/**
 * TenantGuard — Multi-Tenancy Safety Net
 *
 * Use this instead of TenantContext.getCurrentTenant() in new service methods
 * to get a loud, descriptive error if TenantContext was not set (e.g., in async
 * contexts or if JwtFilter did not run).
 *
 * USAGE:
 *   Integer clinicId = TenantGuard.requireClinicId();
 *
 * If TenantContext is null, this throws IllegalStateException immediately —
 * preventing a silent query that would return data from ALL clinics.
 */
public final class TenantGuard {

    private TenantGuard() {
        // Utility class — no instantiation
    }

    /**
     * Returns the current tenant's clinicId.
     * Throws IllegalStateException if TenantContext has not been set.
     *
     * @return the current clinicId (never null)
     * @throws IllegalStateException if TenantContext is null
     */
    public static Integer requireClinicId() {
        Integer clinicId = TenantContext.getCurrentTenant();
        if (clinicId == null) {
            throw new IllegalStateException(
                "[SECURITY VIOLATION] TenantContext clinicId is null. " +
                "This query would expose data from ALL clinics. " +
                "Cause: JwtFilter did not run, or an @Async method did not " +
                "restore TenantContext from its clinicId parameter. " +
                "Fix: Ensure JwtFilter has run for authenticated requests, " +
                "or call TenantContext.setCurrentTenant(clinicId) at the " +
                "start of your @Async method."
            );
        }
        return clinicId;
    }
}
