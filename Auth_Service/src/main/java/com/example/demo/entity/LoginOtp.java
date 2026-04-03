package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "login_otps", indexes = {
        @Index(name = "idx_login_otp_user_purpose", columnList = "user_id,purpose,usedAt"),
        @Index(name = "idx_login_otp_expires_at", columnList = "expiresAt")
})
@Getter
@Setter
@NoArgsConstructor
public class LoginOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String otpHash;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpPurpose purpose = OtpPurpose.LOGIN_CHALLENGE;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public boolean isActive() {
        return usedAt == null && Instant.now().isBefore(expiresAt);
    }
}