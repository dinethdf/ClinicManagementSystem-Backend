package com.dinethdf.ClinicManagementSystem.Controller.Appointment;

import com.dinethdf.ClinicManagementSystem.Service.Appointment.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
@CrossOrigin(origins = "*") // Allow requests from Next.js
public class QueueController {

    @Autowired
    private QueueService queueService;

    // --- SETUP & LIFECYCLE ENDPOINTS ---

    // 1. Initialize Firebase Data (Call this when Schedule is Created)
    @PostMapping("/init")
    public ResponseEntity<String> initializeQueue(@RequestParam Long clinicId,
                                                  @RequestParam String clinicName,
                                                  @RequestParam String clinicContact,
                                                  @RequestParam Long scheduleId,
                                                  @RequestParam String doctorName,
                                                  @RequestParam String specialization,
                                                  @RequestParam String date,
                                                  @RequestParam String time) {

        queueService.initializeSchedule(clinicId, clinicName, clinicContact, scheduleId,
                doctorName, specialization, date, time);
        return ResponseEntity.ok("Queue Initialized in Firebase");
    }

    // 2. Start Session (Doctor Arrived)
    @PostMapping("/start")
    public ResponseEntity<String> startSession(@RequestParam Long clinicId, @RequestParam Long scheduleId) {
        queueService.updateScheduleState(clinicId, scheduleId, "STARTED", null);
        return ResponseEntity.ok("Session Started");
    }

    // 3. Finish Session (Doctor Leaving)
    @PostMapping("/finish")
    public ResponseEntity<String> finishSession(@RequestParam Long clinicId, @RequestParam Long scheduleId) {
        queueService.deleteSchedule(clinicId, scheduleId);
        return ResponseEntity.ok("Session Completed");
    }


    // --- PATIENT FLOW ENDPOINTS ---

    // 5. Add Patient (Call this when Booking Appointment)
    @PostMapping("/add-patient")
    public ResponseEntity<String> addPatient(@RequestParam Long clinicId,
                                             @RequestParam Long scheduleId,
                                             @RequestParam int queueNo,
                                             @RequestParam String patientName) {
        queueService.addPatientToQueue(clinicId, scheduleId, queueNo, patientName);
        return ResponseEntity.ok("Patient Added to Queue #" + queueNo);
    }


    @PostMapping("/call")
    public ResponseEntity<String> callPatient(@RequestParam Long clinicId,
                                              @RequestParam Long scheduleId,
                                              @RequestParam int tokenNo) {
        queueService.callPatient(clinicId, scheduleId, tokenNo);
        return ResponseEntity.ok("Calling Token #" + tokenNo);
    }

    @PostMapping("/complete")
    public ResponseEntity<String> completePatient(@RequestParam Long clinicId,
                                                  @RequestParam Long scheduleId,
                                                  @RequestParam int tokenNo) {
        queueService.completePatient(clinicId, scheduleId, tokenNo);
        return ResponseEntity.ok("Token #" + tokenNo + " marked as COMPLETED");
    }

    @PostMapping("/cancel-patient")
    public ResponseEntity<String> cancelPatient(@RequestParam Long clinicId,
                                                @RequestParam Long scheduleId,
                                                @RequestParam int tokenNo) {
        queueService.cancelAppointment(clinicId, scheduleId, tokenNo);
        return ResponseEntity.ok("Token #" + tokenNo + " marked as CANCELLED");
    }
}