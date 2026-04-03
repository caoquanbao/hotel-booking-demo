package com.example.demo.client;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankVerificationResult {
    private String accountName;
    private boolean valid;
}
