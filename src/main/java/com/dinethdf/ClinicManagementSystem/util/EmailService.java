package com.dinethdf.ClinicManagementSystem.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            // 1. Create a MIME message
            MimeMessage message = mailSender.createMimeMessage();

            // 2. Create the helper.
            // The 'true' flag indicates multipart (needed for HTML)
            // "UTF-8" ensures symbols like currency (£) or emojis display correctly
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);

            // 3. Set the content to HTML
            helper.setText(htmlContent, true);

            // 4. Send
            mailSender.send(message);

        } catch (MessagingException e) {
            // Handle the error (log it)
            System.err.println("Failed to send HTML email to " + to);
            e.printStackTrace();
            // Optional: Throw a custom exception if you want the transaction to rollback
        }
    }
}