package dev.dolu.userservice.models;

public class PayoutInfoResponse {
    private boolean success;
    private String accountName;

    public PayoutInfoResponse() {}
    public PayoutInfoResponse(boolean success, String accountName) {
        this.success = success;
        this.accountName = accountName;
    }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
}

