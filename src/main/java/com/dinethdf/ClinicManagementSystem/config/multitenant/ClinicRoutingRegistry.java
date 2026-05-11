package com.dinethdf.ClinicManagementSystem.config.multitenant;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PHASE 2: In-Memory Registry for Clinic Routing.
 * 
 * This service caches all clinic routing information (subdomain, clinic_code, schema_name, mode)
 * at startup. This prevents the application from querying the master database on every request.
 */
@Service
public class ClinicRoutingRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ClinicRoutingRegistry.class);

    // Maps to allow fast lookup by either clinicCode or subdomain
    private final Map<String, ClinicRoutingInfo> codeToInfoMap = new ConcurrentHashMap<>();
    private final Map<String, ClinicRoutingInfo> subdomainToInfoMap = new ConcurrentHashMap<>();

    private final JdbcTemplate masterJdbcTemplate;

    public ClinicRoutingRegistry(@Qualifier("masterDataSource") DataSource masterDataSource) {
        this.masterJdbcTemplate = new JdbcTemplate(masterDataSource);
    }

    /**
     * Loads all clinics from clinicmsystem_master.clinics into memory.
     * This is called automatically when the application starts.
     */
    @PostConstruct
    public void initialize() {
        refreshRegistry();
    }

    public void refreshRegistry() {
        logger.info("Refreshing Clinic Routing Registry from Master Database...");
        
        String sql = "SELECT id, clinic_code, subdomain, schema_name, tenant_mode FROM clinics WHERE is_deleted = FALSE";

        try {
            masterJdbcTemplate.query(sql, (rs) -> {
                Integer id = rs.getInt("id");
                String code = rs.getString("clinic_code");
                String subdomain = rs.getString("subdomain");
                String schemaName = rs.getString("schema_name");
                boolean isSchemaMode = "SCHEMA".equalsIgnoreCase(rs.getString("tenant_mode"));

                ClinicRoutingInfo info = new ClinicRoutingInfo(id, code, subdomain, schemaName, isSchemaMode);
                
                codeToInfoMap.put(code, info);
                subdomainToInfoMap.put(subdomain, info);
            });

            logger.info("Successfully loaded {} clinics into Routing Registry.", codeToInfoMap.size());
        } catch (Exception e) {
            logger.error("CRITICAL ERROR: Failed to load Clinic Routing Registry from Master DB!", e);
            // In a production scenario, you might want to stop the application here
            // because routing won't work without this data.
        }
    }

    // ── Lookup Methods ───────────────────────────────────────────────────────────

    public ClinicRoutingInfo getByCode(String clinicCode) {
        return codeToInfoMap.get(clinicCode);
    }

    public ClinicRoutingInfo getBySubdomain(String subdomain) {
        return subdomainToInfoMap.get(subdomain);
    }

    public Map<String, ClinicRoutingInfo> getAll() {
        return codeToInfoMap;
    }

    /**
     * Dynamically registers a newly created clinic into the in-memory cache.
     * Called when an Admin creates a new clinic.
     */
    public void register(ClinicRoutingInfo info) {
        if (info != null && info.getClinicCode() != null && info.getSubdomain() != null) {
            codeToInfoMap.put(info.getClinicCode(), info);
            subdomainToInfoMap.put(info.getSubdomain(), info);
            logger.info("Registered new clinic routing info for code: {}", info.getClinicCode());
        }
    }

    /**
     * Persists a new clinic routing record to the master database AND updates the cache.
     */
    public void registerNewClinic(Integer clinicId, String code, String name, String subdomain, 
                                  String schemaName, boolean isSchemaMode, String planCode) {
        
        String sql = "INSERT INTO clinics (id, clinic_code, name, subdomain, schema_name, tenant_mode, plan_code) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try {
            masterJdbcTemplate.update(sql, 
                clinicId, 
                code, 
                name, 
                subdomain, 
                schemaName, 
                isSchemaMode ? "SCHEMA" : "SHARED",
                planCode
            );
            
            ClinicRoutingInfo info = new ClinicRoutingInfo(clinicId, code, subdomain, schemaName, isSchemaMode);
            register(info);
            
        } catch (Exception e) {
            logger.error("Failed to persist new clinic to master database!", e);
            throw new RuntimeException("Master registry update failed", e);
        }
    }
}
