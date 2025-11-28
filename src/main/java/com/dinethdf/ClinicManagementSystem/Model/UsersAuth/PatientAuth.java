package com.dinethdf.ClinicManagementSystem.Model.UsersAuth;

import jakarta.persistence.*;

@Entity
@Table(name = "patientAuth")
public class PatientAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String email;
    private String password;

    private Boolean isEnable;
    private Boolean isTempCredential;

    public Boolean getEnable() {
        return isEnable;
    }
    public void setEnable(Boolean enable) {
        isEnable = enable;
    }



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @ManyToOne
    @JoinColumn(name = "role", referencedColumnName = "id")
    private UserRole role;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }



    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getTempCredential() {
        return isTempCredential;
    }

    public void setTempCredential(Boolean tempCredential) {
        isTempCredential = tempCredential;
    }
    @Override
    public String toString() {
        return "User{" +
                "Id=" + id +
                ", username='" + email + '\'' +
                ", isEnable='" + isEnable + '\'' +
                ", isTempCredential='" + isTempCredential + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                '}';
    }
}
