package com.dinethdf.ClinicManagementSystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class ClinicManagementSystemApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testConcurrentPostRequests() throws InterruptedException {
		int numberOfThreads = 100;
		String apiUrl = "http://localhost:8080/api/patient/details/self";

		String jsonBody = """
		{
		  "patientAuthId": 1,
		  "firstName": "John",
		  "lastName": "Doe",
		  "fullName": "John Doe",
		  "ethnicity": 1,
		  "gender": 1,
		  "maritalStatus": 1,
		  "nic": "123456789V",
		  "dob": "1990-05-15",
		  "address": "123 Main Street",
		  "city": "Colombo",
		  "district": "Colombo",
		  "email": "johndoe@example.com",
		  "mobileNo": "+94712345678",
		  "landNo": "0112345678",
		  "emergencyContactName": "Jane Doe",
		  "emergencyContactRelationship": "Spouse",
		  "emergencyContactMobile": "+94787654321",
		  "emergencyContactEmail": "janedoe@example.com",
		  "bloodGroup": "ab-",
		  "registrationDate": "2025-09-30T10:30:00",
		  "profileImg": "https://example.com/images/john.png",
		  "isActive": true,
		  "isApproved": false,
		  "insuranceId": null,
		  "createdDate": "2025-09-30T10:30:00",
		  "createdBy": "admin",
		  "updatedDate": "2025-09-30T10:30:00",
		  "updatedBy": "admin"
		}
		""";

		ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

		for (int i = 0; i < numberOfThreads; i++) {
			executor.submit(() -> {
				try {
					RestTemplate restTemplate = new RestTemplate();

					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);

					HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

					String response = restTemplate.postForObject(apiUrl, request, String.class);
					System.out.println("Response: " + response);

				} catch (Exception e) {
					System.err.println("Request failed: " + e.getMessage());
				}
			});
		}

		executor.shutdown();
		executor.awaitTermination(30, TimeUnit.SECONDS);
	}
}
