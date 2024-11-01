package dev.dolu.userservice.controller;

import dev.dolu.userservice.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;

@RestController
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/api/test-email")
    public ResponseEntity<String> sendTestEmail(@RequestParam String to) {
        try {
            emailService.sendTestEmail(to);
            return ResponseEntity.ok("Test email sent successfully to " + to);
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body("Failed to send test email: " + e.getMessage());
        }
    }
}
