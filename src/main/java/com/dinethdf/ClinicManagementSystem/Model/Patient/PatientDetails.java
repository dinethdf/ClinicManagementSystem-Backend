package com.dinethdf.ClinicManagementSystem.Model.Patient;

import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.PatientAuth;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_details")
@Data
public class PatientDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "patient_auth_id", referencedColumnName = "id")
    private PatientAuth patientAuth;

    @Column(name = "patientNo", nullable = true, unique = true, length = 100)
    private String patientNo;

    @Column(name = "firstName", nullable = false, length = 50)
    private String firstName;

    @Column(name = "lastName", nullable = false, length = 50)
    private String lastName;

    @Column(name = "fullName", length = 255)
    private String fullName;

    @Column(name = "ethnicity", nullable = false)
    private Integer ethnicity;

    @Column(name = "gender", nullable = false)
    private Integer gender;

    @Column(name = "maritalStatus", nullable = false)
    private Integer maritalStatus;

    @Column(name = "nic", nullable = false, unique = true, length = 20)
    private String nic;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "district", nullable = false)
    private String district;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "mobileNo", nullable = false, length = 20)
    private String mobileNo;

    @Column(name = "landNo", nullable = true, length = 20)
    private String landNo;

    @Column(name = "emergencyContactName", nullable = false, length = 100)
    private String emergencyContactName;

    @Column(name = "emergencyContactRelationship", nullable = false, length = 50)
    private String emergencyContactRelationship;

    @Column(name = "emergencyContactMobile", nullable = false)
    private String emergencyContactMobile;

    @Column(name = "emergencyContactEmail")
    private String emergencyContactEmail;


    @Column(name = "bloodGroup", length = 10)
    private String bloodGroup;

    @Column(name = "registrationDate")
    private LocalDateTime registrationDate;

    @Column(name = "profileImg", length = 255)
    private String profileImg;

    @Column(name = "isActive", nullable = false)
    private Boolean isActive = false;

    @Column(name = "isApproved", nullable = false)
    private Boolean isApproved = false;

    @Column(name = "insurance_id")
    private Integer insuranceId;



    @Column(name = "createdDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "createdBy", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "updatedDate")
    private LocalDateTime updatedDate;

    @Column(name = "updatedBy", length = 50)
    private String updatedBy;


    public enum BloodGroup {
        A_PLUS("A+"), A_MINUS("A-"),
        B_PLUS("B+"), B_MINUS("B-"),
        AB_PLUS("AB+"), AB_MINUS("AB-"),
        O_PLUS("O+"), O_MINUS("O-");

        private final String value;

        BloodGroup(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }


    @Override
    public String toString() {
        return "PatientDetails{" +
                "id=" + id +
                ", patientAuthId=" + patientAuth +
                ", patientNo='" + patientNo + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", ethnicity=" + ethnicity +
                ", gender=" + gender +
                ", maritalStatus=" + maritalStatus +
                ", nic='" + nic + '\'' +
                ", dob=" + dob +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", email='" + email + '\'' +
                ", mobileNo='" + mobileNo + '\'' +
                ", landNo='" + landNo + '\'' +
                ", emergencyContactName='" + emergencyContactName + '\'' +
                ", emergencyContactRelationship='" + emergencyContactRelationship + '\'' +
                ", emergencyContactMobile='" + emergencyContactMobile + '\'' +
                ", emergencyContactEmail='" + emergencyContactEmail + '\'' +
                ", bloodGroup=" + bloodGroup +
                ", registrationDate=" + registrationDate +
                ", profileImg='" + profileImg + '\'' +
                ", isActive=" + isActive +
                ", isApproved=" + isApproved +
                ", insuranceId=" + insuranceId +
                ", createdAt=" + createdDate +
                ", createdBy='" + createdBy + '\'' +
                ", updatedAt=" + updatedDate +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}
