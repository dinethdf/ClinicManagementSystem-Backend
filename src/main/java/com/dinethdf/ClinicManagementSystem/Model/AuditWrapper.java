package com.dinethdf.ClinicManagementSystem.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditWrapper {

    @CreatedDate
    @Column(name = "createdDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @CreatedBy
    @Column(name = "createdBy", nullable = false, updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updatedDate")
    private LocalDateTime updatedDate;

    @LastModifiedBy
    @Column(name = "updatedBy")
    private String updatedBy;

    @Column(name = "isApproved", nullable = false)
    private Boolean isApproved = false;

    @Column(name = "approvedDate")
    private LocalDateTime approvedDate;

    @Column(name = "approvedBy")
    private String approvedBy;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.createdBy = getCurrentUsername();
        this.updatedDate = null;
        this.updatedBy = null;
        this.approvedDate = null;
        this.approvedBy = null;
        this.isApproved = false;
    }

    @PreUpdate
    public void preUpdate() {
        if (this.isApproved) {
            // --- Logic for APPROVING ---
            // If it's being set to 'true' and wasn't approved before
            if (this.approvedDate == null) {
                
                this.approvedDate = LocalDateTime.now();
                this.approvedBy = getCurrentUsername();
            }
        } else {
            // --- Logic for UN-APPROVING ---
            // If it's being set to 'false', clear the approval fields
            this.approvedDate = null;
            this.approvedBy = null;
        }

        // This standard audit logic will run every time
        this.updatedDate = LocalDateTime.now();
        this.updatedBy = getCurrentUsername();

    }

    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !auth.isAuthenticated()) {
                return "System"; // No one is logged in
            }

            String username = auth.getName();

            // Handle the "anonymousUser" case
            if (username == null || "anonymousUser".equalsIgnoreCase(username)) {
                return "anonymousUser";
            }

            return username; // This is the logged-in user from the token

        } catch (Exception ignored) {
            // Fallback in case of any security context error
            return "System";
        }
    }
}
