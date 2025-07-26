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
        int sent = 0;
        for (NewsletterSubscriber subscriber : subscribers) {
            boolean success = emailService.sendEmail(subscriber.getEmail(), subject, content);
            if (success) sent++;
        }
        return ResponseEntity.ok("Newsletter sent to " + sent + " subscribers.");
    }
}

