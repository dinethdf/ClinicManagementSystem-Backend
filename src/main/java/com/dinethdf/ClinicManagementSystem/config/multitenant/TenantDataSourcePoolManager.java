package com.dinethdf.ClinicManagementSystem.config.multitenant;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.Set;

/**
 * PHASE 3: Manages connection pools for SCHEMA mode clinics.
 * 
 * When a request comes in for a SCHEMA clinic (e.g., "clinic_abc123"), this manager
 * checks if a database connection pool already exists for it. If not, it creates
 * a new HikariCP pool on the fly, caches it, and returns it.
 * Uses Caffeine Cache to evict inactive connection pools after 10 minutes of idle time.
 */
@Service
public class TenantDataSourcePoolManager {

    private static final Logger logger = LoggerFactory.getLogger(TenantDataSourcePoolManager.class);

    // Cache of dynamically created data sources. Key = schemaName.
    private final Cache<String, DataSource> resolvedDataSources;

    private final TenantDataSourceProperties properties;

    public TenantDataSourcePoolManager(TenantDataSourceProperties properties) {
        this.properties = properties;
        this.resolvedDataSources = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(10))
                .removalListener((String key, DataSource value, RemovalCause cause) -> {
                    if (value instanceof HikariDataSource) {
                        ((HikariDataSource) value).close();
                        logger.info("Closed database connection pool for schema: {} due to eviction ({})", key, cause);
                    }
                })
                .build();
    }

    /**
     * Retrieves or creates a DataSource for the given schema name.
     */
    public DataSource getDataSource(String schemaName) {
        return resolvedDataSources.get(schemaName, this::createDataSource);
    }

    /**
     * Creates a new HikariCP data source for a specific tenant database.
     */
    private DataSource createDataSource(String schemaName) {
        logger.info("Creating new database connection pool for tenant schema: {}", schemaName);

        HikariConfig config = new HikariConfig();

        // Construct the JDBC URL dynamically based on the schema name
        String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?serverTimezone=Asia/Colombo",
                properties.getHost(), properties.getPort(), schemaName);

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Apply pool settings from properties
        config.setMaximumPoolSize(properties.getPool().getMaximumSize());
        config.setMinimumIdle(properties.getPool().getMinimumIdle());
        config.setIdleTimeout(properties.getPool().getIdleTimeout());
        config.setMaxLifetime(properties.getPool().getMaxLifetime());
        config.setConnectionTimeout(properties.getPool().getConnectionTimeout());
        
        // Pool name for easier debugging/JMX monitoring
        config.setPoolName("HikariPool-" + schemaName);

        return new HikariDataSource(config);
    }

    /**
     * Clears a specific data source from the cache and closes its connections.
     * Useful if a clinic is suspended or its database is moved.
     */
    public void removeDataSource(String schemaName) {
        resolvedDataSources.invalidate(schemaName);
    }

    /**
     * Expose active schema names for verification/testing.
     */
    public Set<String> getActiveSchemas() {
        return resolvedDataSources.asMap().keySet();
    }
}

