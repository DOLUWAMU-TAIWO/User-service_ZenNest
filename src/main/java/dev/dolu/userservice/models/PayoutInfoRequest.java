package dev.dolu.userservice.models;

public class PayoutInfoRequest {
    private String accountNumber;
    private String bankCode;
    private String bvn; // optional

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    public String getBvn() { return bvn; }
    public void setBvn(String bvn) { this.bvn = bvn; }
}

