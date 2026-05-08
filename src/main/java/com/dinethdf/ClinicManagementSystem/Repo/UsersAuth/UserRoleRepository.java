package com.dinethdf.ClinicManagementSystem.Repo.UsersAuth;

import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {

    // Gets ONLY the roles in the list (Admin, Super-Admin)
    List<UserRole> findByNameIn(List<String> roleNames);

    // 🚀 NEW: Gets everything EXCEPT the roles in the list
    List<UserRole> findByNameNotIn(List<String> roleNames);
}