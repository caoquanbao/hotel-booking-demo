package com.example.demo.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtProvider {

    private final SecretKey signingKey;

    public JwtProvider(@Value("${security.jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getEmail(String token) {
        return extractEmail(token);
    }

    public boolean isValidAccessToken(String token, UserDetails userDetails) {
        return extractEmail(token).equals(userDetails.getUsername());
    }

    public boolean isValidAccessToken(String token) {
        extractEmail(token);
        return true;
    }
}
