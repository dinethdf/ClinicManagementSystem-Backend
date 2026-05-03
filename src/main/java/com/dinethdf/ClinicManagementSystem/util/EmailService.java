package com.dinethdf.ClinicManagementSystem.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private static final int MAX_RETRIES = 3;

    @Async("emailTaskExecutor")
    public void sendOtpEmail(String toEmail, String otp) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("your-email@gmail.com");
                message.setTo(toEmail);
                message.setSubject("CureGuard Verification Code");
                message.setText("Your verification code is: " + otp
                        + "\n\nThis code expires in 3 minutes."
                        + "\n\nDo not share this code with anyone.");
                mailSender.send(message);

                System.out.println("OTP Email sent to " + toEmail);
                return; // ✅ Success — exit

            } catch (Exception e) {
                System.err.println("OTP email attempt " + attempt
                        + " failed for " + toEmail + " : " + e.getMessage());

                if (attempt == MAX_RETRIES) {
                    System.err.println("All retries failed for OTP email: " + toEmail);
                }
            }
        }
    }

    @Async("emailTaskExecutor")
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom("your-email@gmail.com");
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(htmlContent, true); // true = HTML

                mailSender.send(message);

                System.out.println("HTML Email sent to " + to);
                return; // ✅ Success — exit

            } catch (MessagingException e) {
                System.err.println("HTML email attempt " + attempt
                        + " failed for " + to + " : " + e.getMessage());

                if (attempt == MAX_RETRIES) {
                    System.err.println("All retries failed for HTML email: " + to);
                }
            }
        }
    }

// Add this method to your existing EmailService.java
// This is called when we are ALREADY in a background thread (@Async)
// So we don't need @Async again — just send directly

    public void sendHtmlEmailSync(String to, String subject, String htmlContent) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom("your-email@gmail.com");
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);

                mailSender.send(message);
                System.out.println("HTML Email sent to " + to);
                return; // ✅ Success

            } catch (MessagingException e) {
                System.err.println("HTML email attempt " + attempt
                        + " failed for " + to + " : " + e.getMessage());
                if (attempt == MAX_RETRIES) {
                    System.err.println("All retries failed for HTML email: " + to);
                }
            }
        }
    }
}