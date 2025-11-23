package com.dinethdf.ClinicManagementSystem.DTO.Patient;

import lombok.Data;

@Data
public class PatientListDTO {
    private Long id;
    private String patientNo;
    private String fullName;
    private String mobileNo;
    private String nic;
    private Integer gender; // You might want to convert this to String (e.g., "Male"/"Female") in a real app
    private int age;
    private Boolean isActive;

    // Constructor matching the fields for easy mapping
    public PatientListDTO(Long id, String patientNo, String firstName, String lastName, String mobileNo, String nic, Integer gender, int age, Boolean isActive) {
        this.id = id;
        this.patientNo = patientNo;
        this.fullName = firstName + " " + lastName; // Combining names for easier display
        this.mobileNo = mobileNo;
        this.nic = nic;
        this.gender = gender;
        this.age = age;
        this.isActive = isActive;
    }
}