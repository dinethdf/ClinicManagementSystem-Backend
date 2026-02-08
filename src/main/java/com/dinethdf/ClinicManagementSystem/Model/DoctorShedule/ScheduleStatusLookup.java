package com.dinethdf.ClinicManagementSystem.Model.DoctorShedule;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "schedule_status_lookup")
@Data
public class ScheduleStatusLookup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String code; // e.g., "SCHEDULED", "COMPLETED"

    @Column(nullable = false, length = 50)
    private String displayName; // e.g., "Scheduled", "Completed"

    @Column(name = "colorCode", length = 20)
    private String colorCode; // e.g., "#28a745"

    @Column(name = "sortOrder")
    private Integer sortOrder;
}