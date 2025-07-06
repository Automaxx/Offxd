package com.secureoffice.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@secureoffice.com}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("SecureOffice - Password Reset Request");
            
            String resetUrl = "http://localhost:5173/reset-password?token=" + resetToken;
            String emailBody = "Dear User,\n\n" +
                "You have requested to reset your password for your SecureOffice account.\n\n" +
                "Please click the following link to reset your password:\n" +
                resetUrl + "\n\n" +
                "This link will expire in 1 hour.\n\n" +
                "If you did not request this password reset, please ignore this email.\n\n" +
                "Best regards,\n" +
                "SecureOffice Team";
            
            message.setText(emailBody);
            
            mailSender.send(message);
        } catch (Exception e) {
            // Log the error but don't throw exception to avoid breaking the flow
            System.err.println("Failed to send password reset email: " + e.getMessage());
        }
    }

    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to SecureOffice!");
            
            String emailBody = "Dear " + firstName + ",\n\n" +
                "Welcome to SecureOffice Communication Hub!\n\n" +
                "Your account has been successfully created. You can now:\n" +
                "- Share and manage documents securely\n" +
                "- Communicate with your team\n" +
                "- Receive real-time notifications\n" +
                "- Access analytics and reports\n\n" +
                "Get started by logging in at: http://localhost:5173\n\n" +
                "If you have any questions, please don't hesitate to contact our support team.\n\n" +
                "Best regards,\n" +
                "SecureOffice Team";
            
            message.setText(emailBody);
            
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }

    public void sendNotificationEmail(String toEmail, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("SecureOffice - " + subject);
            message.setText(content);
            
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send notification email: " + e.getMessage());
        }
    }
}
