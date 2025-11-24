package com.dinethdf.ClinicManagementSystem.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {

    @Value("${sms.api.token}")
    private String apiToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean sendPaymentSms(String recipientPhone, String messageText) {
        try {
            String url = "https://app.text.lk/api/http/sms/send";

            Map<String, String> payload = new HashMap<>();
            payload.put("api_token", apiToken);
            payload.put("recipient", recipientPhone);
            payload.put("sender_id", "CureGuard");
            payload.put("type", "plain");
            payload.put("message", messageText);

//            ResponseEntity<String> response = restTemplate.postForEntity(url, payload, String.class);
//            System.out.println(response);
//            return response.getStatusCode().is2xxSuccessful();
            return false;

        } catch (Exception e) {
            System.err.println("CureGuard API failed: " + e.getMessage());
            return false; // Return false so Controller sets warning message
        }
    }
}