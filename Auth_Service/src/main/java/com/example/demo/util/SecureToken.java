package com.example.demo.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class SecureToken {

    private SecureToken() {}

    public static String generate() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}