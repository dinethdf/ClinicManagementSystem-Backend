package com.dinethdf.ClinicManagementSystem.Repo.SystemConfig;

import com.dinethdf.ClinicManagementSystem.Model.SystemConfig.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SystemConfigRepo extends JpaRepository<SystemConfig, String> {
    Optional<SystemConfig> findByKey(String key);
}