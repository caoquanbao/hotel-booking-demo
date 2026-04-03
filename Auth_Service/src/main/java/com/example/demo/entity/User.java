package com.example.demo.entity;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Chỉ dùng cho LOCAL.
     * Với GOOGLE user có thể null (vì login bằng OAuth).
     */
    @Column(nullable = true)
    private String password;

    /**
     * LOCAL hoặc GOOGLE
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(nullable = false)
    private String role;

    /**
     * Verify email (thường chỉ bắt buộc với LOCAL)
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    /**
     * Mốc thời gian đổi mật khẩu gần nhất.
     * Dùng để revoke access token: nếu jwt.iat < passwordChangedAt => reject.
     */
    @Column(nullable = true)
    private Instant passwordChangedAt;

    /**
     * (Optional) nếu muốn revoke tất cả token ngay lập tức
     */
    @Column(nullable = false)
    @Builder.Default
    private Long tokenVersion = 0L;

    // =========================
    // 🔐 BRUTE FORCE + ACCOUNT LOCK
    // =========================

    /**
     * Số lần login sai liên tiếp
     */
    @Column(nullable = false)
    @Builder.Default
    private int failedLoginCount = 0;

    /**
     * Nếu != null và > now => account đang bị lock
     */
    @Column(nullable = true)
    private Instant lockUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuthStatus authStatus = AuthStatus.NORMAL;

    @Column(nullable = false)
    @Builder.Default
    private int failedPasswordAttempts = 0;

    @Column(nullable = false)
    @Builder.Default
    private int failedOtpAttempts = 0;

    @Column(nullable = true)
    private Instant otpCooldownUntil;

    public boolean isLocked() {
        return lockUntil != null && lockUntil.isAfter(Instant.now());
    }

    public enum AuthProvider {
        LOCAL, GOOGLE
    }
}
