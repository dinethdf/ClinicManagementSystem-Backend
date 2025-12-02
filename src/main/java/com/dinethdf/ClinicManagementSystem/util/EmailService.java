package com.dinethdf.ClinicManagementSystem.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("your-email@gmail.com"); // Configure in properties
            message.setTo(toEmail);
            message.setSubject("CureGuard Verification Code");
            message.setText("Your verification code is: " + otp + "\n\nThis code expires in 3 minutes.");

            mailSender.send(message);
            System.out.println("Email sent to " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}