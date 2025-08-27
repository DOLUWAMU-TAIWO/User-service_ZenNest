package dev.dolu.userservice.controller;

import dev.dolu.userservice.models.NewsletterSubscriber;
import dev.dolu.userservice.repository.NewsletterSubscriberRepository;
import dev.dolu.userservice.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/newsletter")
public class NewsletterController {
    @Autowired
    private NewsletterSubscriberRepository newsletterSubscriberRepository;
    @Autowired
    private EmailService emailService;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestParam("email") String email) {
        if (newsletterSubscriberRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("Email already subscribed.");
        }
        newsletterSubscriberRepository.save(new NewsletterSubscriber(email));
        return ResponseEntity.ok("Subscribed successfully.");
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNewsletter(@RequestParam("subject") String subject,
                                            @RequestParam("content") String content) {
        List<NewsletterSubscriber> subscribers = newsletterSubscriberRepository.findAll();
        List<String> emails = subscribers.stream().map(NewsletterSubscriber::getEmail).toList();

        // Send individual emails instead of bulk email for privacy
        int successCount = 0;
        for (String email : emails) {
            boolean sent = emailService.sendEmail(email, subject, content);
            if (sent) {
                successCount++;
            }
        }

        if (successCount > 0) {
            return ResponseEntity.ok("Newsletter sent to " + successCount + " of " + emails.size() + " subscribers.");
        } else {
            return ResponseEntity.status(500).body("Failed to send newsletter to subscribers.");
        }
    }
}
