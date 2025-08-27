package dev.dolu.userservice.controller;

import dev.dolu.userservice.models.ContactRequest;
import dev.dolu.userservice.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final EmailService emailService;
    private final String contactRecipient;

    public ContactController(EmailService emailService, @Value("${contact.recipient.email:admin@zennest.africa}") String contactRecipient) {
        this.emailService = emailService;
        this.contactRecipient = contactRecipient;
    }

    @PostMapping
    public ResponseEntity<?> contactUs(@RequestBody ContactRequest request) {
        String subject = "[Contact Form] " + request.getSubject();
        String content = "<p><strong>Name:</strong> " + request.getName() + "</p>" +
                         "<p><strong>Email:</strong> " + request.getEmail() + "</p>" +
                         "<p><strong>Message:</strong></p><p>" + request.getMessage() + "</p>";
        boolean sent = emailService.sendEmail(contactRecipient, subject, content);
        if (sent) {
            return ResponseEntity.ok("Your message has been sent successfully.");
        } else {
            return ResponseEntity.status(500).body("Failed to send your message. Please try again later.");
        }
    }
}

