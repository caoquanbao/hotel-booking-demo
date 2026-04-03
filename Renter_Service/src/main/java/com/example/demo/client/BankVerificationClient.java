package com.example.demo.client;

public interface BankVerificationClient {
    BankVerificationResult verifyAccount(String bankCode, String accountNumber);
}
