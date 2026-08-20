package com.dinethdf.ClinicManagementSystem.util;

import com.dinethdf.ClinicManagementSystem.Service.Clinic.ClinicService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {

//    @Value("${sms.api.token}")
    private String apiToken;

    private final RestTemplate restTemplate = new RestTemplate();

    // ✅ Single shared ObjectMapper — created once, thread-safe for reads
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ClinicService clinicService;

    // ─── Gateway Type Constants ────────────────────────────────────────────────
    public static final String SMS_GATEWAY = "SMS_GATEWAY";
    public static final String SMS_APP     = "SMS_APP";
    public static final String SMS_NONE    = "NONE";

    // ─── Secret Key Names ──────────────────────────────────────────────────────
    private static final String SECRET_APP_API_KEY    = "smsAppApiKey";
    private static final String SECRET_GATEWAY_TOKEN  = "smsGatewayApiToken";
    private static final String SECRET_GATEWAY_SENDER = "smsGatewaySenderId";

    // ─── Main Entry Point ──────────────────────────────────────────────────────

    /**
     * Sends an SMS using the gateway configured for this clinic.
     * TenantContext must be set by the caller before invoking this method.
     */
    public boolean sendDynamicSms(String recipientPhone, String messageText, String gatewayType) {
        if (gatewayType == null) {
            gatewayType = SMS_GATEWAY;
        }

        if (SMS_NONE.equalsIgnoreCase(gatewayType)) {
            return true; // Silently skip sending
        }

        // ✅ Load secrets ONCE, shared across both gateway paths — no double DB/cache hit
        Map<String, String> secrets = loadClinicSecrets();

        if (SMS_APP.equalsIgnoreCase(gatewayType)) {
            return sendViaApp(recipientPhone, messageText, secrets);
        }

        // Default: SMS_GATEWAY (text.lk)
        return sendViaGateway(recipientPhone, messageText, secrets);
    }

    /**
     * Direct call to text.lk — used when gateway type is already known to be SMS_GATEWAY,
     * or as a safe fallback for any unrecognized gateway type.
     */
    public boolean sendDetailsSms(String recipientPhone, String messageText) {
        Map<String, String> secrets = loadClinicSecrets();
        return sendViaGateway(recipientPhone, messageText, secrets);
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    /**
     * Loads and parses the clinic's encrypted secrets from the cache.
     * Returns an empty map if no secrets exist or if parsing fails.
     */
    private Map<String, String> loadClinicSecrets() {
        try {
            String secretsJson = clinicService.getCurrentEncryptedSecrets();
            if (secretsJson != null && !secretsJson.isBlank()) {
                return objectMapper.readValue(secretsJson, new TypeReference<Map<String, String>>() {});
            }
        } catch (Exception e) {
            System.err.println("CureGuard: Could not load clinic secrets, using system defaults. Reason: " + e.getMessage());
        }
        return new HashMap<>();
    }

    /**
     * Sends via your custom Vercel SMS API (SMS_APP).
     * Requires 'smsAppApiKey' to be stored in the clinic's encrypted secrets vault.
     */
    private boolean sendViaApp(String recipientPhone, String messageText, Map<String, String> secrets) {
        String apiKey = secrets.get(SECRET_APP_API_KEY);

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("CureGuard SMS_APP Failed: 'smsAppApiKey' not found in clinic secrets. "
                    + "Save it via: PUT /api/clinic/info/secrets → { \"smsAppApiKey\": \"sk_live_...\" }");
            return false;
        }

        try {
            String url = "https://sms-backend-brown-sigma.vercel.app/v1/sms/send";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", apiKey);

            Map<String, String> body = new HashMap<>();
            body.put("number", recipientPhone);
            body.put("message", messageText);
            body.put("sim", "1");

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            System.out.println("SMS_APP Response [" + recipientPhone + "]: " + response.getBody());
            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            System.err.println("CureGuard SMS_APP Failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Sends via text.lk (SMS_GATEWAY).
     * Uses clinic-specific token/sender ID if stored in secrets,
     * otherwise falls back to system-wide defaults from application.properties.
     */
    private boolean sendViaGateway(String recipientPhone, String messageText, Map<String, String> secrets) {
        String token    = getOrDefault(secrets, SECRET_GATEWAY_TOKEN,  this.apiToken);
        String senderId = getOrDefault(secrets, SECRET_GATEWAY_SENDER, "CureGuard");

        try {
            String url = "https://app.text.lk/api/http/sms/send"
                    + "?api_token={api_token}&recipient={recipient}&sender_id={sender_id}&type={type}&message={message}";

            Map<String, String> params = new HashMap<>();
            params.put("api_token", token);
            params.put("recipient", recipientPhone);
            params.put("sender_id", senderId);
            params.put("type",      "plain");
            params.put("message",   messageText);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, params);

            System.out.println("SMS_GATEWAY Response [" + recipientPhone + "]: " + response.getBody());
            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            System.err.println("CureGuard SMS_GATEWAY Failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper: returns the value from the secrets map if present and non-blank,
     * otherwise returns the system-level default.
     */
    private String getOrDefault(Map<String, String> map, String key, String defaultValue) {
        String value = map.get(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
}