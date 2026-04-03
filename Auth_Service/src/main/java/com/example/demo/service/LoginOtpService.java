package com.example.demo.service;

import com.example.demo.entity.LoginOtp;
import com.example.demo.entity.OtpPurpose;
import com.example.demo.entity.User;
import com.example.demo.repository.LoginOtpRepository;
import com.example.demo.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LoginOtpService {

    private final LoginOtpRepository loginOtpRepository;

    @Value("${auth.otp.secret}")
    private String otpSecret;

    @Transactional
    public void ensureOtpSent(User user, String ip, String userAgent, boolean forceNew) {
        if (!forceNew) {
            boolean hasActive = loginOtpRepository
                    .findLatestActive(user.getId(), OtpPurpose.LOGIN_CHALLENGE)
                    .isPresent();
            if (hasActive) {
                return;
            }
        }

        String rawOtp = OtpUtil.generateNumericOtp(6);
        String otpHash = OtpUtil.hashOtp(rawOtp, user.getId(), otpSecret);

        LoginOtp entity = new LoginOtp();
        entity.setUser(user);
        entity.setOtpHash(otpHash);
        entity.setPurpose(OtpPurpose.LOGIN_CHALLENGE);
        entity.setCreatedAt(Instant.now());
        entity.setExpiresAt(Instant.now().plusSeconds(300));
        loginOtpRepository.save(entity);
    }

    @Transactional
    public boolean verifyOtp(User user, String otp) {
        LoginOtp active = loginOtpRepository
                .findLatestActive(user.getId(), OtpPurpose.LOGIN_CHALLENGE)
                .orElse(null);

        if (active == null || active.getExpiresAt().isBefore(Instant.now())) {
            return false;
        }

        String expectedHash = OtpUtil.hashOtp(otp, user.getId(), otpSecret);
        boolean matched = expectedHash.equals(active.getOtpHash());
        if (matched) {
            active.setUsedAt(Instant.now());
            loginOtpRepository.save(active);
        }
        return matched;
    }
}
