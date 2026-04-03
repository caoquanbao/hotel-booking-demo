package com.example.demo.service;

import com.example.demo.client.BankVerificationClient;
import com.example.demo.client.BankVerificationResult;
import com.example.demo.client.NotificationClient;
import com.example.demo.dto.NotificationMetadata;
import com.example.demo.dto.NotificationRecipient;
import com.example.demo.dto.NotificationRequest;
import com.example.demo.dto.RenterVerificationRequest;
import com.example.demo.dto.VerificationResult;
import com.example.demo.entity.RenterVerification;
import com.example.demo.entity.VerificationStatus;
import com.example.demo.exception.ExternalServiceException;
import com.example.demo.repository.RenterVerificationRepository;
import com.example.demo.validator.CCCDValidator;
import com.example.demo.validator.MSTValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RenterVerificationService {

    private static final int MAX_RETRY = 3;
    private static final Duration BANK_TIMEOUT = Duration.ofSeconds(2);

    private final RenterVerificationRepository verificationRepository;
    private final CCCDValidator cccdValidator;
    private final MSTValidator mstValidator;
    private final BankVerificationClient bankVerificationClient;
    private final NotificationClient notificationClient;

    @Transactional
    public VerificationResult verifyRenter(RenterVerificationRequest request) {
        RenterVerification verification = RenterVerification.builder()
                .userId(request.getUserId())
                .cccdNumber(request.getCccd())
                .mstNumber(request.getMst())
                .bankCode(request.getBankCode())
                .bankAccountNumber(request.getBankAccount())
                .verificationStatus(VerificationStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        verification = verificationRepository.save(verification);

        if (!cccdValidator.isValidCCCD(request.getCccd())) {
            return reject(verification, "Invalid CCCD");
        }
        verification.setVerificationStatus(VerificationStatus.CCCD_VALID);
        verification = verificationRepository.save(verification);

        if (!mstValidator.isValidMST(request.getMst())) {
            return reject(verification, "Invalid MST");
        }
        verification.setVerificationStatus(VerificationStatus.MST_VALID);
        verification = verificationRepository.save(verification);

        BankVerificationResult bankResult = verifyBankWithRetry(request.getBankCode(), request.getBankAccount());
        if (!bankResult.isValid()) {
            return reject(verification, "Invalid bank account");
        }

        verification.setAccountHolderName(bankResult.getAccountName());
        verification.setVerificationStatus(VerificationStatus.BANK_VERIFIED);
        verification = verificationRepository.save(verification);

        verification.setVerificationStatus(VerificationStatus.VERIFIED);
        verification = verificationRepository.save(verification);

        sendRenterVerificationResultSafely(
                verification.getUserId(),
                verification.getAccountHolderName(),
                verification.getVerificationStatus().name(),
                "Renter verification completed successfully"
        );

        return VerificationResult.builder()
                .status(verification.getVerificationStatus())
                .accountHolderName(verification.getAccountHolderName())
                .build();
    }

    private BankVerificationResult verifyBankWithRetry(String bankCode, String accountNumber) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                return CompletableFuture
                        .supplyAsync(() -> bankVerificationClient.verifyAccount(bankCode, accountNumber))
                        .orTimeout(BANK_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
                        .join();
            } catch (Exception ex) {
                log.error("Bank verification attempt {} failed for bankCode {} account {}: {}",
                        attempt, bankCode, accountNumber, ex.getMessage(), ex);
                lastException = ex;
            }
        }

        throw new ExternalServiceException("Bank verification API failed after 3 retries", lastException);
    }

    private VerificationResult reject(RenterVerification verification, String reason) {
        verification.setVerificationStatus(VerificationStatus.REJECTED);
        verificationRepository.save(verification);

        sendRenterVerificationResultSafely(
                verification.getUserId(),
                verification.getAccountHolderName(),
                VerificationStatus.REJECTED.name(),
                reason
        );

        return VerificationResult.builder()
                .status(VerificationStatus.REJECTED)
                .accountHolderName(null)
                .build();
    }

    private void sendRenterVerificationResultSafely(Long renterId, String renterName, String status, String reason) {
        try {
            notificationClient.send(NotificationRequest.builder()
                    .type("RENTER_VERIFICATION_RESULT")
                    .recipient(NotificationRecipient.builder().build())
                    .payload(Map.of(
                            "renterId", renterId,
                            "renterName", renterName == null || renterName.isBlank() ? "Renter-" + renterId : renterName,
                            "status", status,
                            "reason", reason
                    ))
                    .metadata(NotificationMetadata.builder()
                            .idempotencyKey("renter-verification-" + renterId + "-" + status)
                            .build())
                    .build());
        } catch (Exception exception) {
            log.warn("Failed to send renter verification result notification for renter {}", renterId, exception);
        }
    }
}
