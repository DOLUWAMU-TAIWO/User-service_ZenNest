package dev.dolu.userservice.models;

import jakarta.persistence.*;

@Entity
@Table(name = "newsletter_subscribers")
public class NewsletterSubscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    public NewsletterSubscriber() {}
    public NewsletterSubscriber(String email) { this.email = email; }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

