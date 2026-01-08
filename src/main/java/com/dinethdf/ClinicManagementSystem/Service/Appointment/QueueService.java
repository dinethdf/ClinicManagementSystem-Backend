package com.dinethdf.ClinicManagementSystem.Service.Appointment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class QueueService {


    public void initializeSchedule(Long clinicId, String clinicName, String clinicContact,
                                   Long scheduleId, String doctorName, String specialization,
                                   String date, String time) {

        DatabaseReference clinicRef = FirebaseDatabase.getInstance().getReference("clinics/" + clinicId);
        DatabaseReference scheduleRef = clinicRef.child("schedules").child(String.valueOf(scheduleId));

        // 1. Set Clinic Details (Updates main clinic node)
        Map<String, Object> clinicUpdates = new HashMap<>();
        clinicUpdates.put("clinicName", clinicName);
        clinicUpdates.put("clinicContactNo", clinicContact);
        clinicRef.updateChildrenAsync(clinicUpdates);

        // 2. Set Schedule Details & Default State
        Map<String, Object> scheduleUpdates = new HashMap<>();
        scheduleUpdates.put("doctorName", doctorName);
        scheduleUpdates.put("specialization", specialization);
        scheduleUpdates.put("date", date);
        scheduleUpdates.put("time", time);

        // Default Status Fields
        scheduleUpdates.put("scheduleStatus", "SCHEDULED"); // Initial state
        scheduleUpdates.put("currentServing", 0);
        scheduleUpdates.put("startedAt", "");

        scheduleRef.updateChildrenAsync(scheduleUpdates);
    }


    public void updateScheduleState(Long clinicId, Long scheduleId, String status, String reason) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("clinics/" + clinicId + "/schedules/" + scheduleId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("scheduleStatus", status); // "STARTED", "COMPLETED", "CANCELLED"

        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));

        if ("STARTED".equals(status)) {
            updates.put("startedAt", currentTime);
        }

        ref.updateChildrenAsync(updates);
    }

    public void deleteSchedule(Long clinicId, Long scheduleId) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("clinics/" + clinicId + "/schedules/" + scheduleId);

        // This deletes the node and all data under it
        ref.removeValueAsync();
    }

    public void addPatientToQueue(Long clinicId, Long scheduleId, int queueNo, String patientName) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("clinics/" + clinicId + "/schedules/" + scheduleId);

        Map<String, Object> patientData = new HashMap<>();
        patientData.put("queueNo", queueNo);
        patientData.put("patientName", patientName);
        patientData.put("status", "WAITING"); // Default status for new booking

        // Add to the 'appointments' list under the specific queue number
        ref.child("appointments").child(String.valueOf(queueNo)).setValueAsync(patientData);
    }


    public void callPatient(Long clinicId, Long scheduleId, int queueNo) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("clinics/" + clinicId + "/schedules/" + scheduleId);

        // 1. Update the big number on the screen
        ref.child("currentServing").setValueAsync(queueNo);

        // 2. Mark THIS patient as "ONGOING"
        ref.child("appointments").child(String.valueOf(queueNo))
                .child("status").setValueAsync("ONGOING");
    }


    public void completePatient(Long clinicId, Long scheduleId, int queueNo) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("clinics/" + clinicId + "/schedules/" + scheduleId);

        // Mark specific patient as COMPLETED
        ref.child("appointments").child(String.valueOf(queueNo))
                .child("status").setValueAsync("COMPLETED");
    }

    public void cancelAppointment(Long clinicId, Long scheduleId, int queueNo) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("clinics/" + clinicId + "/schedules/" + scheduleId + "/appointments/" + queueNo);

        // Just update status. Number remains occupied.
        ref.child("status").setValueAsync("CANCELLED");
    }
}