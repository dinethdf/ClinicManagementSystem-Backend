package com.dinethdf.ClinicManagementSystem.config.multitenant;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * PHASE 3: Complete Database Configuration
 * 
 * 1. Configures the Master Registry database.
 * 2. Configures the Shared Legacy database.
 * 3. Wires the TenantRoutingDataSource as the Primary data source for the application.
 */
@Configuration
public class MasterDataSourceConfig {

    // ── 1. Master Registry Database ──────────────────────────────────────────────
    @Bean
    @ConfigurationProperties("master.datasource")
    public DataSourceProperties masterDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "masterDataSource")
    public DataSource masterDataSource() {
        return masterDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    // ── 2. Shared Legacy Database ────────────────────────────────────────────────
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties sharedDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "sharedDataSource")
    public DataSource sharedDataSource() {
        return sharedDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    // ── 3. Dynamic Routing Database (PRIMARY) ────────────────────────────────────
    /**
     * This is the Primary data source that Spring Data JPA/Hibernate will use.
     * It intercepts queries and delegates them to either the sharedDataSource or
     * a dynamic Hikari pool created by TenantDataSourcePoolManager.
     */
    @Bean
    @Primary
    public DataSource dataSource(
            TenantDataSourcePoolManager poolManager,
            DataSource sharedDataSource) {

        TenantRoutingDataSource routingDataSource = new TenantRoutingDataSource(poolManager);
        
        // The shared database acts as the default fallback
        routingDataSource.setDefaultTargetDataSource(sharedDataSource);
        
        // AbstractRoutingDataSource requires this map, even if empty, to initialize
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("shared", sharedDataSource); // register shared statically
        routingDataSource.setTargetDataSources(targetDataSources);
        
        // This is crucial: applies the default target
        routingDataSource.afterPropertiesSet();
        
        return routingDataSource;
    }
}
