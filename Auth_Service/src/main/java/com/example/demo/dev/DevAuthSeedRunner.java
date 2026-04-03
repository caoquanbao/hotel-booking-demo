package com.example.demo.dev;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DevAuthSeedRunner implements CommandLineRunner {

    private final DevUserSeedService devUserSeedService;
    private final DevTokenSeedService devTokenSeedService;

    @Override
    public void run(String... args) {
        User normalUser = devUserSeedService.seedNormalUser();
        User unverifiedUser = devUserSeedService.seedUnverifiedUser();
        User otpUser = devUserSeedService.seedOtpUser();

        String verifyTokenRaw = devTokenSeedService.seedEmailVerificationToken(unverifiedUser);
        String otpRaw = devTokenSeedService.seedLoginOtp(otpUser);
        String resetTokenRaw = devTokenSeedService.seedPasswordResetToken(normalUser);

        log.info("DEV ONLY auth seed completed");
        log.info("DEV USER: {} / {}", normalUser.getEmail(), DevSeedProperties.RAW_PASSWORD);
        log.info("DEV USER: {} / {}", unverifiedUser.getEmail(), DevSeedProperties.RAW_PASSWORD);
        log.info("DEV USER: {} / {}", otpUser.getEmail(), DevSeedProperties.RAW_PASSWORD);
        log.info("DEV EMAIL VERIFY TOKEN (raw): {}", verifyTokenRaw);
        log.info("DEV OTP (raw): {}", otpRaw);
        log.info("DEV RESET TOKEN (raw): {}", resetTokenRaw);
    }
}
