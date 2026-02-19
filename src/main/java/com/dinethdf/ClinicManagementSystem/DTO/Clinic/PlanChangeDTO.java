package com.dinethdf.ClinicManagementSystem.DTO.Clinic;


import lombok.Data;

@Data
public class PlanChangeDTO {
    // The code for the new plan (e.g., "PLAN_PRO", "PLAN_ENTERPRISE")
    private String newPlanCode;

    // Optional: You might want to add a billing cycle if your app supports it
    // private String billingCycle; // "MONTHLY" or "YEARLY"
}