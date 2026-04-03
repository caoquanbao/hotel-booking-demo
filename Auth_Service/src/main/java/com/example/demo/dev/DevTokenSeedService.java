package com.example.demo.dev;

import com.example.demo.entity.EmailVerificationToken;
import com.example.demo.entity.LoginOtp;
import com.example.demo.entity.OtpPurpose;
import com.example.demo.entity.PasswordResetToken;
import com.example.demo.entity.User;
import com.example.demo.repository.EmailVerificationTokenRepository;
import com.example.demo.repository.LoginOtpRepository;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.util.OtpUtil;
import com.example.demo.util.SecureToken;
import com.example.demo.util.TokenHash;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DevTokenSeedService {

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final LoginOtpRepository loginOtpRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${auth.otp.secret}")
    private String otpSecret;

    @Transactional
    public String seedEmailVerificationToken(User user) {
        emailVerificationTokenRepository.deleteByUser_Id(user.getId());

        String rawToken = SecureToken.generate();
        EmailVerificationToken token = EmailVerificationToken.builder()
                .tokenHash(TokenHash.sha256Hex(rawToken))
                .user(user)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();

        emailVerificationTokenRepository.save(token);
        return rawToken;
    }

    @Transactional
    public String seedLoginOtp(User user) {
        loginOtpRepository.deleteByUser_Id(user.getId());

        String rawOtp = OtpUtil.generateNumericOtp(6);
        LoginOtp otp = new LoginOtp();
        otp.setUser(user);
        otp.setOtpHash(OtpUtil.hashOtp(rawOtp, user.getId(), otpSecret));
        otp.setPurpose(OtpPurpose.LOGIN_CHALLENGE);
        otp.setCreatedAt(Instant.now());
        otp.setExpiresAt(Instant.now().plus(5, ChronoUnit.MINUTES));

        loginOtpRepository.save(otp);
        return rawOtp;
    }

    @Transactional
    public String seedPasswordResetToken(User user) {
        passwordResetTokenRepository.deleteByUser_Id(user.getId());

        String rawToken = SecureToken.generate();
        PasswordResetToken token = PasswordResetToken.builder()
                .tokenHash(TokenHash.sha256Hex(rawToken))
                .user(user)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .build();

        passwordResetTokenRepository.save(token);
        return rawToken;
    }
}
