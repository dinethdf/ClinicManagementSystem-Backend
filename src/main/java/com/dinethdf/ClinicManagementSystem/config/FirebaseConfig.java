package com.dinethdf.ClinicManagementSystem.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            // Put your 'service-account.json' in src/main/resources folder
            InputStream serviceAccount =
                    getClass().getClassLoader().getResourceAsStream("service-account.json");

            if (serviceAccount == null) {
                throw new RuntimeException("service-account.json not found in classpath");
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://cureguard-hms-default-rtdb.asia-southeast1.firebasedatabase.app/") // Replace with your URL
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}