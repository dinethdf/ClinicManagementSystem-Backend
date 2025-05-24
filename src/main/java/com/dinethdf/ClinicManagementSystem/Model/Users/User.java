package com.dinethdf.ClinicManagementSystem.Model.Users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class User {

    @Id
    private int Id;
    private String email;
    private String name;
    private String password;

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


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return "User{" +
                "Id=" + Id +
                ", username='" + email + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                '}';
    }
}
