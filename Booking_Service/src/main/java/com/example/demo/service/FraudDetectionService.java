package com.example.demo.service;

import com.example.demo.entity.ReviewUser;
import com.example.demo.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final ReviewRepository reviewRepository;

    public double applySpamPenalties(double baseTrustScore,
                                     ReviewUser user,
                                     Long hotelId,
                                     String reviewText,
                                     String ipAddress,
                                     Instant now) {
        double trustScore = baseTrustScore;

        List<String> recentTexts = reviewRepository.findRecentReviewTexts(user.getId(), hotelId, PageRequest.of(0, 5));
        for (String oldText : recentTexts) {
            double similarity = textSimilarity(reviewText, oldText);
            if (similarity > 0.9d) {
                trustScore *= 0.3d;
                break;
            }
        }

        long reviewsSameIp = reviewRepository.countByIpAddressAndCreatedAtAfter(ipAddress, now.minus(Duration.ofMinutes(10)));
        if (reviewsSameIp > 5) {
            trustScore *= 0.2d;
        }

        long accountAgeDays = Duration.between(user.getAccountCreatedAt(), now).toDays();
        int reviewCount = safeInt(user.getReviewCount());
        if (accountAgeDays < 2 && reviewCount > 3) {
            trustScore *= 0.1d;
        }

        return clamp01(trustScore);
    }

    private double textSimilarity(String a, String b) {
        Set<String> left = tokens(a);
        Set<String> right = tokens(b);
        if (left.isEmpty() || right.isEmpty()) {
            return 0.0d;
        }

        Set<String> intersection = new HashSet<>(left);
        intersection.retainAll(right);

        Set<String> union = new HashSet<>(left);
        union.addAll(right);

        return (double) intersection.size() / union.size();
    }

    private Set<String> tokens(String text) {
        Set<String> out = new HashSet<>();
        if (text == null) {
            return out;
        }
        String normalized = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        for (String token : normalized.split("\\s+")) {
            if (!token.isBlank()) {
                out.add(token);
            }
        }
        return out;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private double clamp01(double value) {
        if (value < 0) return 0;
        return Math.min(value, 1.0d);
    }
}
