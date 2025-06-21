package dev.dolu.userservice.models;

import java.util.UUID;

public class UserDTO {
    private UUID id;
    private String username;
    private String email;
    private PayoutInfo payoutInfo;
    private boolean openVisitations;
    private boolean isPaymentVerified;

    // Constructor
    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.payoutInfo = user.getPayoutInfo();
        this.openVisitations = user.isOpenVisitations();
        this.isPaymentVerified = user.isPaymentVerified();
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

    public PayoutInfo getPayoutInfo() {
        return payoutInfo;
    }

    public void setPayoutInfo(PayoutInfo payoutInfo) {
        this.payoutInfo = payoutInfo;
    }

    public boolean isOpenVisitations() {
        return openVisitations;
    }

    public void setOpenVisitations(boolean openVisitations) {
        this.openVisitations = openVisitations;
    }

    public boolean isPaymentVerified() {
        return isPaymentVerified;
    }

    public void setPaymentVerified(boolean paymentVerified) {
        isPaymentVerified = paymentVerified;
    }
}