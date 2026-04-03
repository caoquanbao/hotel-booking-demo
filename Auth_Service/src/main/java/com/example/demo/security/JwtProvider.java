package com.example.demo.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private final SecretKey signingKey;
    private final long accessExpMs;

    public JwtProvider(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-exp-ms:900000}") long accessExpMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpMs = accessExpMs;
    }

    public String generateAccessToken(String subjectEmail) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiration = new Date(now + accessExpMs);

        log.info("Generating access token for {}", subjectEmail);
        log.info("nowMillis = {}", now);
        log.info("issuedAt = {}", issuedAt);
        log.info("expiration = {}", expiration);

        String accessToken = Jwts.builder()
                .setSubject(subjectEmail)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        log.info("accessToken = {}", accessToken);
        return accessToken;
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
