package com.example.demo.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public final class OtpUtil {

    private static final SecureRandom RND = new SecureRandom();

    private OtpUtil() {}

    public static String generateNumericOtp(int length) {
        int bound = (int) Math.pow(10, length);
        int floor = (int) Math.pow(10, length - 1);
        int num = floor + RND.nextInt(bound - floor);
        return String.valueOf(num);
    }

    public static String hashOtp(String otp, Long userId, String secret) {
        String raw = userId + ":" + otp + ":" + secret;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            log.error("Failed to hash OTP for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Cannot hash OTP", e);
        }
    }
}
