package com.dinethdf.ClinicManagementSystem.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenantContext {
    private static final Logger logger = LoggerFactory.getLogger(TenantContext.class);
    private static final ThreadLocal<Integer> currentTenant = new ThreadLocal<>();

    public static void setCurrentTenant(Integer tenantId) {
        logger.debug("Setting tenant to {}", tenantId);
        currentTenant.set(tenantId);
    }

    public static Integer getCurrentTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        logger.debug("Clearing tenant context");
        currentTenant.remove();
    }
}