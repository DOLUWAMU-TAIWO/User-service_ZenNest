package dev.dolu.userservice.controller;

import dev.dolu.userservice.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EmailController {


    @Value("${email.service.api-key}")
    private String apiKey;

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/test-email")
    public ResponseEntity<String> sendTestEmail(@RequestParam String to) {



        System.out.println("YOUR api" + apiKey);
        boolean emailSent = emailService.sendTestEmail(to); // ✅ Calls sendTestEmail()

        if (emailSent) {
            return ResponseEntity.ok("Test email sent successfully to " + to);
        } else {
            return ResponseEntity.status(500).body("Failed to send test email.");
        }
    }
}