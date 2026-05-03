package com.dinethdf.ClinicManagementSystem.DTO.Auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemAccountStatusDTO {

    // True if a UserAuth record exists for this staff member
    private boolean hasAccount;

    // Maps to the 'email' field in UserAuth (what they use to log in)
    private String username;

    // Kept for future-proofing your frontend, currently defaults to false
    private boolean twoFactorEnabled;

    // The inverse of UserAuth's 'isEnable' field
    private boolean isSuspended;
}