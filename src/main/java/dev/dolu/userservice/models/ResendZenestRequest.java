package dev.dolu.userservice.models;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

// DTOs for Zenest endpoints
public class ResendZenestRequest {
    @NotNull(message = "Email must not be null")
    @Email(message = "Email must be a valid email address")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}