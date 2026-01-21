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

    public boolean sendDetailsSms(String recipientPhone, String messageText) {
        try {
            // 1. Define URL with {placeholders} for the parameters
            // This keeps the URL clean and tells RestTemplate where to put the data
            String url = "https://app.text.lk/api/http/sms/send?api_token={api_token}&recipient={recipient}&sender_id={sender_id}&type={type}&message={message}";

            // 2. Put details into a Map (Just like your 2nd example)
            Map<String, String> params = new HashMap<>();
            params.put("api_token", apiToken);
            params.put("recipient", recipientPhone);
            params.put("sender_id", "CureGuard");
            params.put("type", "plain");
            params.put("message", messageText); // RestTemplate will handle spaces/encoding automatically

            // 3. Use GET method (The one that works)
            // Passing 'params' as the 3rd argument automatically fills the {placeholders}
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, params);

            System.out.println("SMS Response: " + response.getBody());
            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            System.err.println("CureGuard SMS Failed: " + e.getMessage());
            return false;
        }
    }
}