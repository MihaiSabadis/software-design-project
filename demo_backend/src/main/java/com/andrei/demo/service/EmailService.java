package com.andrei.demo.service;

import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String toEmail, String resetCode){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("playworthy.noreply@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Your PlayWorthy Password Reset Code");
        message.setText("Hello!\n\n" +
                "You requested to reset your password.\n" +
                "Here is your 6-digit reset code: " + resetCode + "\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Thanks,\nThe PlayWorthy Team");

        mailSender.send(message);
    }

    public void sendPasswordChangeConfirmation(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@playworthy.com");
        message.setTo(toEmail);
        message.setSubject("Your Password Was Changed");
        message.setText("Hello,\n\nThis is a confirmation that your PlayWorthy account password was just successfully changed.\n\nIf you did not do this, please contact support immediately.\n\nThanks,\nThe PlayWorthy Team");

        mailSender.send(message);
    }
}
