package com.dinethdf.ClinicManagementSystem.config.multitenant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * PHASE 3: Properties for dynamically created tenant databases.
 * Reads the tenant.datasource.* properties from application.properties.
 */
@Component
@ConfigurationProperties(prefix = "tenant.datasource")
public class TenantDataSourceProperties {

    private String host;
    private int port;
    private String username;
    private String password;
    private final Pool pool = new Pool();

    public static class Pool {
        private int maximumSize = 3;
        private int minimumIdle = 1;
        private long idleTimeout = 120000;
        private long maxLifetime = 1800000;
        private long connectionTimeout = 20000;

        // Getters and Setters
        public int getMaximumSize() { return maximumSize; }
        public void setMaximumSize(int maximumSize) { this.maximumSize = maximumSize; }
        public int getMinimumIdle() { return minimumIdle; }
        public void setMinimumIdle(int minimumIdle) { this.minimumIdle = minimumIdle; }
        public long getIdleTimeout() { return idleTimeout; }
        public void setIdleTimeout(long idleTimeout) { this.idleTimeout = idleTimeout; }
        public long getMaxLifetime() { return maxLifetime; }
        public void setMaxLifetime(long maxLifetime) { this.maxLifetime = maxLifetime; }
        public long getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(long connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    }

    // Getters and Setters
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Pool getPool() { return pool; }
}
