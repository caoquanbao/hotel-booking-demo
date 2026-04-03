package com.example.demo.service;

import com.example.demo.client.NotificationClient;
import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.ForgotPasswordRequest;
import com.example.demo.dto.NotificationMetadata;
import com.example.demo.dto.NotificationRecipient;
import com.example.demo.dto.NotificationRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.entity.EmailVerificationToken;
import com.example.demo.entity.PasswordResetToken;
import com.example.demo.entity.User;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.exception.InvalidTokenException;
import com.example.demo.exception.TokenAlreadyUsedException;
import com.example.demo.exception.TokenExpiredException;
import com.example.demo.repository.EmailVerificationTokenRepository;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.SecureToken;
import com.example.demo.util.TokenHash;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountSecurityService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationClient notificationClient;

    private final String frontendBaseUrl = "http://localhost:3000";
    private final String backendBaseUrl = "http://localhost:8080";

    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        userRepository.findByEmail(req.getEmail()).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUser_Id(user.getId());

            String raw = SecureToken.generate();
            String hash = TokenHash.sha256Hex(raw);

            PasswordResetToken token = PasswordResetToken.builder()
                    .tokenHash(hash)
                    .user(user)
                    .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                    .build();

            passwordResetTokenRepository.save(token);

            String link = frontendBaseUrl + "/reset-password?token=" + raw;

            notificationClient.send(NotificationRequest.builder()
                    .type("RESET_PASSWORD")
                    .recipient(NotificationRecipient.builder()
                            .email(user.getEmail())
                            .name(user.getEmail())
                            .build())
                    .payload(Map.of(
                            "resetLink", link,
                            "expiredInMinutes", 15
                    ))
                    .metadata(NotificationMetadata.builder()
                            .idempotencyKey("reset-password-user-" + user.getId() + "-token-" + hash)
                            .build())
                    .build());
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        String hash = TokenHash.sha256Hex(req.getToken());

        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hash)
                .orElseThrow(InvalidTokenException::new);

        if (token.getUsedAt() != null) {
            throw new TokenAlreadyUsedException();
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenExpiredException();
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(token);

        refreshTokenRepository.deleteByUser_Id(user.getId());
    }

    @Transactional
    public void registerAndSendVerifyLink(String email, String rawPassword) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .provider(User.AuthProvider.LOCAL)
                .role("USER")
                .emailVerified(false)
                .build();

        userRepository.save(user);
        emailVerificationTokenRepository.deleteByUser_Id(user.getId());

        String raw = SecureToken.generate();
        String hash = TokenHash.sha256Hex(raw);

        EmailVerificationToken token = EmailVerificationToken.builder()
                .tokenHash(hash)
                .user(user)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();

        emailVerificationTokenRepository.save(token);

        String link = backendBaseUrl + "/api/auth/verify-email?token=" + raw;

        notificationClient.send(NotificationRequest.builder()
                .type("VERIFY_EMAIL")
                .recipient(NotificationRecipient.builder()
                        .email(user.getEmail())
                        .name(user.getEmail())
                        .build())
                .payload(Map.of(
                        "verificationLink", link,
                        "expiredInMinutes", 24 * 60
                ))
                .metadata(NotificationMetadata.builder()
                        .idempotencyKey("verify-email-user-" + user.getId() + "-token-" + hash)
                        .build())
                .build());
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        String hash = TokenHash.sha256Hex(rawToken);

        EmailVerificationToken token = emailVerificationTokenRepository.findByTokenHash(hash)
                .orElseThrow(InvalidTokenException::new);

        if (token.getUsedAt() != null) {
            throw new TokenAlreadyUsedException();
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenExpiredException();
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        emailVerificationTokenRepository.save(token);
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest req) {
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        refreshTokenRepository.deleteByUser_Id(user.getId());
    }
}
