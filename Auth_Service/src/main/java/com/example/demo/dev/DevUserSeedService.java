package com.example.demo.dev;

import com.example.demo.entity.AuthStatus;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DevUserSeedService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User seedNormalUser() {
        return seedLocalUser(
                DevSeedProperties.NORMAL_EMAIL,
                true,
                AuthStatus.NORMAL
        );
    }

    public User seedUnverifiedUser() {
        return seedLocalUser(
                DevSeedProperties.UNVERIFIED_EMAIL,
                false,
                AuthStatus.NORMAL
        );
    }

    public User seedOtpUser() {
        return seedLocalUser(
                DevSeedProperties.OTP_EMAIL,
                true,
                AuthStatus.OTP_REQUIRED
        );
    }

    private User seedLocalUser(String email, boolean emailVerified, AuthStatus authStatus) {
        return userRepository.findByEmail(email)
                .map(existing -> updateExistingUser(existing, emailVerified, authStatus))
                .orElseGet(() -> createUser(email, emailVerified, authStatus));
    }

    private User updateExistingUser(User user, boolean emailVerified, AuthStatus authStatus) {
        user.setPassword(passwordEncoder.encode(DevSeedProperties.RAW_PASSWORD));
        user.setProvider(User.AuthProvider.LOCAL);
        user.setRole("USER");
        user.setEmailVerified(emailVerified);
        user.setAuthStatus(authStatus);
        user.setFailedLoginCount(0);
        user.setLockUntil(null);
        user.setFailedPasswordAttempts(0);
        user.setFailedOtpAttempts(0);
        user.setOtpCooldownUntil(null);
        return userRepository.save(user);
    }

    private User createUser(String email, boolean emailVerified, AuthStatus authStatus) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(DevSeedProperties.RAW_PASSWORD))
                .provider(User.AuthProvider.LOCAL)
                .role("USER")
                .emailVerified(emailVerified)
                .authStatus(authStatus)
                .failedLoginCount(0)
                .failedPasswordAttempts(0)
                .failedOtpAttempts(0)
                .tokenVersion(0L)
                .build();
        return userRepository.save(user);
    }
}
