package dev.dolu.userservice.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id")  // Connects token to a specific user
    private User user;

    private LocalDateTime expiryDate;

    // Default constructor for JPA
    public VerificationToken() {}

    // Constructor for token creation
    public VerificationToken(String token, User user, LocalDateTime expiryDate) {
        this.token = token;
        this.user = user;
        this.expiryDate = LocalDateTime.now().plusMinutes(5); // 5-minute expiry
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getToken() { return token; }
    public User getUser() { return user; }
    public LocalDateTime getExpiryDate() { return expiryDate; }

    public void setToken(String token) { this.token = token; }
    public void setUser(User user) { this.user = user; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
}
