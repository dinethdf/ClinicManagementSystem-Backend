package com.dinethdf.ClinicManagementSystem.DTO.Patient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class PatientListDTO {
    private Long id;
    private String patientNo;
    private String firstName;
    private String lastName;
    private String mobileNo;
    private String nic;
    private String city;
    private LocalDate dob;
//    private boolean isActive;

    @JsonProperty("isActive")
    private boolean isActive;

    // ✅ JPA Constructor - Matches the Repo Query order EXACTLY
    public PatientListDTO(Long id, String patientNo, String firstName, String lastName,
                          String mobileNo, String nic, String city, LocalDate dob, boolean isActive) {
        this.id = id;
        this.patientNo = patientNo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobileNo = mobileNo;
        this.nic = nic;
        this.city = city;
        this.dob = dob;
        this.isActive = isActive;
    }

    public int getAge() {
        if (this.dob == null) return 0;
        return java.time.Period.between(this.dob, java.time.LocalDate.now()).getYears();
    }
}