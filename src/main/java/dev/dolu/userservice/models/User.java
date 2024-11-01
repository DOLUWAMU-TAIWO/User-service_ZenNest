package dev.dolu.userservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "email")})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password should be at least 8 characters")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;


    @Column(nullable = false)
    private Boolean enabled = false;



    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // No-argument constructor required by JPA
    public User() {}

    // Constructor with arguments for creating instances
    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = false;
    }
    // Constructors, getters, and setters

    // Getter and setter for 'enabled'
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
    }
    public Boolean getEnabled() {
        return enabled;
    }
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    // Custom method to check if the user's role is admin'

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
