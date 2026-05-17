package com.dinethdf.ClinicManagementSystem.config.multitenant;

import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

/**
 * PHASE 3: The engine of the dynamic routing.
 * 
 * This class intercepts every database query made by Hibernate/JPA.
 * It checks the TenantContext for the current schema name.
 * - If "shared" (or null), it uses the default shared clinicmsystem database.
 * - If "clinic_xxx", it fetches the dynamic pool from the TenantDataSourcePoolManager.
 */
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private static final Logger logger = LoggerFactory.getLogger(TenantRoutingDataSource.class);
    
    private final TenantDataSourcePoolManager poolManager;

    public TenantRoutingDataSource(TenantDataSourcePoolManager poolManager) {
        this.poolManager = poolManager;
    }

    /**
     * We don't use the standard lookup key approach because our tenant databases
     * are created dynamically at runtime, not known at application startup.
     */
    @Override
    protected Object determineCurrentLookupKey() {
        // Return null to allow fallback to the default data source if needed
        return null;
    }

    /**
     * Override the determination logic to dynamically switch between the 
     * static shared database and the dynamically created tenant databases.
     */
    @Override
    protected DataSource determineTargetDataSource() {
        String schemaName = TenantContext.getCurrentSchema();
        
        System.out.println("ROUTER DEBUG: determineTargetDataSource called. Current Schema: " + schemaName);
        
        // Safety check + legacy support: route to shared database
        if (schemaName == null || "shared".equalsIgnoreCase(schemaName)) {
            System.out.println("ROUTER DEBUG: Routing to DEFAULT (shared)");
            // This will use the default target data source configured in RoutingDataSourceConfig
            return super.determineTargetDataSource();
        }

        // It's a SCHEMA mode clinic: fetch or create its dedicated connection pool
        System.out.println("ROUTER DEBUG: Routing to ISOLATED schema: " + schemaName);
        logger.debug("Routing database query to schema: {}", schemaName);
        return poolManager.getDataSource(schemaName);
    }
}
