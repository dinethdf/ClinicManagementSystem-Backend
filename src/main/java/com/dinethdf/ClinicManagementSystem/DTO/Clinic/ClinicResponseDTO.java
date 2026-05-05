package com.dinethdf.ClinicManagementSystem.DTO.Clinic;

import com.dinethdf.ClinicManagementSystem.Model.Clinic.Clinic;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClinicResponseDTO {
    private Integer id;
    private String name;
    private String subdomain;
    private String address;
    private String city;
    private String district;

    // --- UPDATED CONTACT FIELDS ---
    private String mobileNo1;
    private String mobileNo2;
    private String landlineNo;

    private String email;
    private String logoUrl;
    private String status;
    private String subscriptionPlan;
    private LocalDate expiryDate;

    private String appointmentBookedSmsGateway;
    private String appointmentConfirmSmsGateway;
    private String otpSmsGateway;

    // ✅ STATIC MAPPER UPDATED
    public static ClinicResponseDTO fromEntity(Clinic clinic) {
        ClinicResponseDTO dto = new ClinicResponseDTO();
        dto.setId(clinic.getId());
        dto.setName(clinic.getName());
        dto.setSubdomain(clinic.getSubdomain());
        dto.setAddress(clinic.getAddress());
        dto.setCity(clinic.getCity());
        dto.setDistrict(clinic.getDistrict());

        // --- MAP NEW FIELDS ---
        dto.setMobileNo1(clinic.getMobileNo1());
        dto.setMobileNo2(clinic.getMobileNo2());
        dto.setLandlineNo(clinic.getLandlineNo());

        dto.setEmail(clinic.getEmail());
        dto.setLogoUrl(clinic.getLogoUrl());

        // Handle Enums & Plan Codes
        dto.setStatus(clinic.getStatus().name());
        dto.setSubscriptionPlan(clinic.getPlanCode());

        // Handle Settings
        if (clinic.getSettings() != null) {
            dto.setAppointmentBookedSmsGateway(clinic.getSettings().getAppointmentBookedSmsGateway());
            dto.setAppointmentConfirmSmsGateway(clinic.getSettings().getAppointmentConfirmSmsGateway());
            dto.setOtpSmsGateway(clinic.getSettings().getOtpSmsGateway());
        }

        // Handle Date Conversion
        if (clinic.getSubscriptionEndsAt() != null) {
            dto.setExpiryDate(clinic.getSubscriptionEndsAt().toLocalDate());
        } else if (clinic.getTrialEndsAt() != null) {
            dto.setExpiryDate(clinic.getTrialEndsAt().toLocalDate());
        }

        return dto;
    }
}