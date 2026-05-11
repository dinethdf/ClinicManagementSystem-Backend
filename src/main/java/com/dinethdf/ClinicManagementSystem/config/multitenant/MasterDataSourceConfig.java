package com.dinethdf.ClinicManagementSystem.config.multitenant;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * PHASE 2: Configuration for the Master Database.
 * 
 * This database (clinicmsystem_master) only contains the routing information.
 * It is separate from the actual clinic data databases.
 */
@Configuration
public class MasterDataSourceConfig {

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

    /**
     * Define the Primary DataSource properties for the Shared database (existing clinicmsystem).
     * We mark this as @Primary so existing standard JPA components continue to work 
     * until we implement the full routing logic in Phase 3/4.
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        return dataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
}
