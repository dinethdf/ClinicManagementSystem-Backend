package com.dinethdf.ClinicManagementSystem.Repo.UsersAuth;

import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<UserAuth, Integer> {

    UserAuth findByEmailAndClinicId(String email, Integer clinicId);

    // 🚀 NEW: Find by the Staff/Details ID
    UserAuth findByDetailsIdAndClinicId(Long detailsId, Integer clinicId);
}
