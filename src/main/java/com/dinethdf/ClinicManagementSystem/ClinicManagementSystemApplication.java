package com.dinethdf.ClinicManagementSystem;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.util.TimeZone;

@EnableCaching
@SpringBootApplication
public class ClinicManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClinicManagementSystemApplication.class, args);
	}

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Colombo"));

		System.out.println("Global Timezone set to: Asia/Colombo set");
	}

}