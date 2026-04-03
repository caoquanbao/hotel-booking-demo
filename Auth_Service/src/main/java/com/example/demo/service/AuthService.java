package com.example.demo.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.OtpResendRequest;
import com.example.demo.dto.OtpVerifyRequest;
import com.example.demo.dto.RefreshRequest;
import com.example.demo.entity.AuthStatus;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.SecurityEventType;
import com.example.demo.entity.User;
import com.example.demo.exception.EmailNotVerifiedException;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.exception.InvalidOtpException;
import com.example.demo.exception.InvalidRefreshTokenException;
import com.example.demo.exception.OtpCooldownException;
import com.example.demo.exception.OtpNotRequiredException;
import com.example.demo.exception.OtpRequiredException;
import com.example.demo.exception.RefreshTokenExpiredException;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtProvider;
import com.example.demo.util.TokenHash;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final LoginBruteForceService loginBruteForceService;
    private final AccountLockService accountLockService;
    private final LoginOtpService loginOtpService;
    private final SecurityAuditService securityAuditService;

    private final long REFRESH_DAYS = 7;

    private static final int MAX_FAILED_PASSWORD = 5;
    private static final int MAX_FAILED_OTP = 5;
    private static final int OTP_COOLDOWN_MINUTES = 10;

    // =========================================================
    // LOGIN
    // =========================================================

    @Transactional
    public AuthResponse login(LoginRequest request, String ip, String userAgent) {

        loginBruteForceService.checkAllowed(ip, request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        accountLockService.assertNotLocked(user);

        // ================= OTP_REQUIRED =================
        if (user.getAuthStatus() == AuthStatus.OTP_REQUIRED) {

            if (user.getOtpCooldownUntil() != null &&
                    Instant.now().isBefore(user.getOtpCooldownUntil())) {
                throw new OtpCooldownException();
            }

            loginOtpService.ensureOtpSent(user, ip, userAgent, false);

            securityAuditService.log(
                    user.getId(),
                    SecurityEventType.OTP_REQUIRED,
                    ip,
                    userAgent,
                    null
            );

            throw new OtpRequiredException();
        }

        // ================= PASSWORD CHECK =================
        boolean matches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!matches) {

            loginBruteForceService.onFailed(ip, request.getEmail());
            accountLockService.onLoginFailure(user);

            securityAuditService.log(
                    user.getId(),
                    SecurityEventType.LOGIN_FAILED,
                    ip,
                    userAgent,
                    "Wrong password"
            );

            int failed = user.getFailedPasswordAttempts() + 1;
            user.setFailedPasswordAttempts(failed);

            if (failed >= MAX_FAILED_PASSWORD) {

                user.setAuthStatus(AuthStatus.OTP_REQUIRED);
                user.setFailedOtpAttempts(0);
                user.setOtpCooldownUntil(null);

                loginOtpService.ensureOtpSent(user, ip, userAgent, true);

                securityAuditService.log(
                        user.getId(),
                        SecurityEventType.OTP_REQUIRED,
                        ip,
                        userAgent,
                        "Triggered after 5 failed attempts"
                );

                throw new OtpRequiredException();
            }

            throw new InvalidCredentialsException();
        }

        // ================= EMAIL VERIFIED =================
        if (user.getProvider() == User.AuthProvider.LOCAL &&
                !user.isEmailVerified()) {

            loginBruteForceService.onFailed(ip, request.getEmail());

            securityAuditService.log(
                    user.getId(),
                    SecurityEventType.LOGIN_FAILED,
                    ip,
                    userAgent,
                    "Email not verified"
            );

            throw new EmailNotVerifiedException();
        }

        // ================= SUCCESS =================
        loginBruteForceService.onSuccess(ip, request.getEmail());
        accountLockService.onLoginSuccess(user);

        user.setFailedPasswordAttempts(0);

        securityAuditService.log(
                user.getId(),
                SecurityEventType.LOGIN_SUCCESS,
                ip,
                userAgent,
                null
        );

        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        String refreshTokenRaw = issueRefreshToken(user);

        return new AuthResponse(accessToken, refreshTokenRaw);
    }

    // =========================================================
    // VERIFY OTP
    // =========================================================

    @Transactional
    public AuthResponse verifyOtp(OtpVerifyRequest request,
                                  String ip,
                                  String userAgent) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidOtpException::new);

        if (user.getAuthStatus() != AuthStatus.OTP_REQUIRED) {
            throw new OtpNotRequiredException();
        }

        if (user.getOtpCooldownUntil() != null &&
                Instant.now().isBefore(user.getOtpCooldownUntil())) {
            throw new OtpCooldownException();
        }

        boolean ok = loginOtpService.verifyOtp(user, request.getOtp());

        if (!ok) {

            securityAuditService.log(
                    user.getId(),
                    SecurityEventType.OTP_FAILED,
                    ip,
                    userAgent,
                    null
            );

            int failed = user.getFailedOtpAttempts() + 1;
            user.setFailedOtpAttempts(failed);

            if (failed >= MAX_FAILED_OTP) {

                user.setOtpCooldownUntil(
                        Instant.now().plus(Duration.ofMinutes(OTP_COOLDOWN_MINUTES))
                );

                securityAuditService.log(
                        user.getId(),
                        SecurityEventType.ACCOUNT_LOCKED,
                        ip,
                        userAgent,
                        "Too many wrong OTP"
                );

                throw new OtpCooldownException();
            }

            throw new InvalidOtpException();
        }

        // OTP SUCCESS
        user.setAuthStatus(AuthStatus.NORMAL);
        user.setFailedPasswordAttempts(0);
        user.setFailedOtpAttempts(0);
        user.setOtpCooldownUntil(null);

        securityAuditService.log(
                user.getId(),
                SecurityEventType.OTP_SUCCESS,
                ip,
                userAgent,
                null
        );

        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        String refreshTokenRaw = issueRefreshToken(user);

        return new AuthResponse(accessToken, refreshTokenRaw);
    }

    // =========================================================
    // RESEND OTP
    // =========================================================

    @Transactional
    public void resendOtp(OtpResendRequest request,
                          String ip,
                          String userAgent) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(OtpNotRequiredException::new);

        if (user.getAuthStatus() != AuthStatus.OTP_REQUIRED) {
            throw new OtpNotRequiredException();
        }

        if (user.getOtpCooldownUntil() != null &&
                Instant.now().isBefore(user.getOtpCooldownUntil())) {
            throw new OtpCooldownException();
        }

        loginOtpService.ensureOtpSent(user, ip, userAgent, false);
    }

    // =========================================================
    // REFRESH
    // =========================================================

    public AuthResponse refresh(RefreshRequest request) {

        if (request.getRefreshToken() == null ||
                request.getRefreshToken().isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        String tokenHash = TokenHash.sha256Hex(request.getRefreshToken());

        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (stored.isRevoked()) {
            throw new InvalidRefreshTokenException();
        }

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new RefreshTokenExpiredException();
        }

        User user = stored.getUser();

        if (user.isLocked()) {
            throw new com.example.demo.exception.AccountLockedException();
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        securityAuditService.log(
                user.getId(),
                SecurityEventType.REFRESH_ROTATED,
                null,
                null,
                null
        );

        String newAccessToken = jwtProvider.generateAccessToken(user.getEmail());
        String newRefreshTokenRaw = issueRefreshToken(user);

        return new AuthResponse(newAccessToken, newRefreshTokenRaw);
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    @Transactional
    public void logout(Long userId) {

        refreshTokenRepository.deleteByUser_Id(userId);

        securityAuditService.log(
                userId,
                SecurityEventType.LOGOUT,
                null,
                null,
                null
        );
    }

    // =========================================================
    // TOKEN HELPERS
    // =========================================================

    private String issueRefreshToken(User user) {

        String raw = generateSecureToken();
        String hash = TokenHash.sha256Hex(raw);

        RefreshToken entity = RefreshToken.builder()
                .tokenHash(hash)
                .user(user)
                .expiresAt(Instant.now().plus(REFRESH_DAYS, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        refreshTokenRepository.save(entity);
        return raw;
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
