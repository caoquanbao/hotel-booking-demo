package com.example.demo.client;

import org.springframework.stereotype.Component;

@Component
public class VietQrBankVerificationClient implements BankVerificationClient {

    @Override
    public BankVerificationResult verifyAccount(String bankCode, String accountNumber) {
        // Mock VietQR behavior for sandbox/dev.
        // In production, replace with real HTTP integration.
        if (bankCode == null || bankCode.isBlank() || accountNumber == null || accountNumber.isBlank()) {
            return BankVerificationResult.builder().valid(false).build();
        }

        if (!bankCode.matches("[A-Z]{2,6}") || !accountNumber.matches("\\d{8,20}")) {
            return BankVerificationResult.builder().valid(false).build();
        }

        String accountName = "NGUYEN VAN A";
        return BankVerificationResult.builder()
                .valid(true)
                .accountName(accountName)
                .build();
    }
}
