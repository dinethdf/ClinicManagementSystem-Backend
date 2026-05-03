package com.dinethdf.ClinicManagementSystem.Service.UsersAuth;

import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserRole;

import com.dinethdf.ClinicManagementSystem.Repo.UsersAuth.UserRoleRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    public UserRoleService(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    public List<UserRole> getAllRoles() {
        return userRoleRepository.findAll();
    }

    public List<UserRole> getAdminRoles() {
        List<String> targetRoles = Arrays.asList("ADMIN", "SUPER-ADMIN");
        return userRoleRepository.findByNameIn(targetRoles);
    }

    public List<UserRole> getStandardRoles() {
        List<String> excludedRoles = Arrays.asList("ADMIN", "SUPER-ADMIN");
        return userRoleRepository.findByNameNotIn(excludedRoles);
    }
}