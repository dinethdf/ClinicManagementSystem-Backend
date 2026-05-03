package com.dinethdf.ClinicManagementSystem.Controller.UsersAuth;

import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserRole;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.UserRoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class UserRoleController {

    private final UserRoleService userRoleService;

    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

//    @PreAuthorize("hasAuthority('SUPER-ADMIN')")
//    @GetMapping
//    public ResponseEntity<List<UserRole>> getAllRoles() {
//        return ResponseEntity.ok(userRoleService.getAllRoles());
//    }


    @PreAuthorize("hasAnyAuthority('SUPER-ADMIN')")
    @GetMapping("/admins")
    public ResponseEntity<List<UserRole>> getAdminRoles() {
        return ResponseEntity.ok(userRoleService.getAdminRoles());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER-ADMIN')")
    @GetMapping("/standard")
    public ResponseEntity<List<UserRole>> getStandardRoles() {
        return ResponseEntity.ok(userRoleService.getStandardRoles());
    }
}