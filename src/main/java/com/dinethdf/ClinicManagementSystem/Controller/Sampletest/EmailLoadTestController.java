package com.dinethdf.ClinicManagementSystem.Controller.Sampletest;

import com.dinethdf.ClinicManagementSystem.util.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class EmailLoadTestController {

    @Autowired
    private EmailService emailService;

    /**
     * Trigger a load test by sending 100 emails.
     * If a list of emails is provided in the body, it will use them (cycling through if less than 100).
     * If no list is provided, it will return an error.
     */
    @PostMapping("/email-load")
    public ResponseEntity<Map<String, Object>> triggerLoadTest(@RequestBody(required = false) List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Please provide a list of emails in the request body.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        int totalToSend = 100;
        int sentCount = 0;

        String subject = "CureGuard System Load Test";
        String htmlContent = "<h1>System Performance Test</h1>" +
                "<p>This is an automated email sent during a performance load test for the CureGuard Clinic Management System.</p>" +
                "<p>Time Sent: " + java.time.LocalDateTime.now() + "</p>" +
                "<p>If you received this, the async email queue is working correctly.</p>";

        for (int i = 0; i < totalToSend; i++) {
            // Cycle through the provided emails if there are fewer than 100
            String targetEmail = emails.get(i % emails.size());
            emailService.sendHtmlEmail(targetEmail, subject, htmlContent);
            sentCount++;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Load test triggered");
        response.put("totalQueued", sentCount);
        response.put("uniqueRecipients", emails.size());
        response.put("message", "100 emails have been queued in the async thread pool.");

        return ResponseEntity.ok(response);
    }
}
