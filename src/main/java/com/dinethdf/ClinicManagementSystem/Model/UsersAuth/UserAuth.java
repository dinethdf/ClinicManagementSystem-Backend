package com.dinethdf.ClinicManagementSystem.Model.UsersAuth;

import jakarta.persistence.*;

@Entity
@Table(name = "userAuth")
public class UserAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int Id;
    private String email;
    private String password;

    private Long detailsId;

    private Boolean isEnable;

    @Column(name = "clinic_id", nullable = false)
    private Integer clinicId;


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
        return Id;
    }

    public void setId(int id) {
        Id = id;
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

    public Long getDetailsId() {
        return detailsId;
    }

    public void setDetailsId(Long id) {
        this.detailsId = id;
    }

    public Integer getClinicId() { return clinicId; }
    public void setClinicId(Integer clinicId) { this.clinicId = clinicId; }

    @Override
    public String toString() {
        return "User{" +
                "Id=" + Id +
                ", clinicID='" + clinicId + '\'' +
                ", username='" + email + '\'' +
                ", isEnable='" + isEnable + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                '}';
    }
}
