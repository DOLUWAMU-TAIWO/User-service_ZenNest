package dev.dolu.userservice.models;

import jakarta.persistence.*;

@Entity
public class UserMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String device;
    private String sipAddress;
    private String userAgent;
    private String ipAddress;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }

    public String getSipAddress() { return sipAddress; }
    public void setSipAddress(String sipAddress) { this.sipAddress = sipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}

