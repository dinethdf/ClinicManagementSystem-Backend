package com.dinethdf.ClinicManagementSystem.Model.Doctor;

import com.dinethdf.ClinicManagementSystem.Model.CommonUserDetails;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserAuth;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "doctor")
@Data
@EqualsAndHashCode(callSuper = true)
public class Doctor extends CommonUserDetails {

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "auth_id")
    private UserAuth userAuth;

    @Column(name = "nameWithInitials", length = 150)
    private String nameWithInitials;

    @Column(name = "slmcNumber", unique = true, length = 50)
    private String slmcNumber;

    @Column(name = "specialization", length = 100)
    private String specialization;

    @Column(name = "qualification", length = 100)
    private String qualification;

    @Column(name = "experienceYears", length = 10)
    private Integer experienceYears;

    public enum AvailabilityStatus {
        AVAILABLE("AVAILABLE"), ON_LEAVE("ON_LEAVE"),
        UNAVAILABLE("UNAVAILABLE");
        private final String value;

        AvailabilityStatus(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @Column(name = "registrationDate")
    private LocalDateTime registrationDate;

}
