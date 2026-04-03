package com.example.demo.dto;

import com.example.demo.entity.VerificationStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerificationResult {
    private VerificationStatus status;
    private String accountHolderName;
}
