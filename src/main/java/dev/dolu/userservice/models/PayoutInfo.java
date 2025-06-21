package dev.dolu.userservice.models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Embeddable
public class PayoutInfo {
    private String accountNumber;          // e.g., "0123456789"
    private String bankCode;               // e.g., "058" for GTBank
    private String bankName;               // optional, for UI display
    private String accountHolderName;      // resolved via Paystack
    private String recipientCode;          // e.g., "RCP_xxxx" from Paystack (for payouts)
    private String bvn;                    // optional, for additional verification
    private String emailForPayouts;        // could be used for receipts/alerts
    @Column(name = "payout_verified")
    private boolean verified;              // true if BVN + bank are resolved
    private LocalDateTime lastUpdated;     // for auditing
    private String currency = "NGN";       // in case you add USD later

    // Getters and setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }
    public String getRecipientCode() { return recipientCode; }
    public void setRecipientCode(String recipientCode) { this.recipientCode = recipientCode; }
    public String getBvn() { return bvn; }
    public void setBvn(String bvn) { this.bvn = bvn; }
    public String getEmailForPayouts() { return emailForPayouts; }
    public void setEmailForPayouts(String emailForPayouts) { this.emailForPayouts = emailForPayouts; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
