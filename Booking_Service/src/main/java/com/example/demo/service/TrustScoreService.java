package com.example.demo.service;

import com.example.demo.entity.ReviewUser;
import com.example.demo.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TrustScoreService {

    private final ReviewRepository reviewRepository;

    public double calculateTrustScore(ReviewUser user) {
        return calculateTrustScore(user, "", Instant.now());
    }

    public double calculateTrustScore(ReviewUser user, String reviewText, Instant now) {
        double ageScore = calculateAgeScore(user, now);
        double bookingScore = calculateBookingScore(user);
        double verifiedScore = calculateVerifiedScore(user);
        double qualityScore = calculateQualityScore(user, reviewText);
        double ipScore = mapIpScore(user.getIpReputationScore());
        double burstScore = calculateBurstScore(user, now);

        double trustScore =
                0.25d * bookingScore +
                0.20d * verifiedScore +
                0.15d * ageScore +
                0.15d * qualityScore +
                0.15d * ipScore +
                0.10d * burstScore;

        return clamp01(trustScore);
    }

    public double convertTrustToWeight(double trustScore) {
        double safeTrust = clamp01(trustScore);
        return 0.1d + safeTrust * 2.0d;
    }

    private double calculateAgeScore(ReviewUser user, Instant now) {
        long ageDays = Duration.between(user.getAccountCreatedAt(), now).toDays();
        return clamp01((double) ageDays / 365d);
    }

    private double calculateBookingScore(ReviewUser user) {
        return clamp01(safeInt(user.getTotalBookings()) / 10d);
    }

    private double calculateVerifiedScore(ReviewUser user) {
        double verifiedStays = safeInt(user.getVerifiedStays());
        double reviewCount = Math.max(safeInt(user.getReviewCount()), 1);
        return clamp01(verifiedStays / reviewCount);
    }

    private double calculateQualityScore(ReviewUser user, String reviewText) {
        double normalizedLength = clamp01((double) safeText(reviewText).length() / 500d);
        double helpfulRatio = clamp01((double) safeInt(user.getHelpfulVotesReceived()) / Math.max(safeInt(user.getReviewCount()), 1));
        return clamp01(0.5d * normalizedLength + 0.5d * helpfulRatio);
    }

    private double mapIpScore(Double ipReputationScore) {
        double score = ipReputationScore == null ? 1.0d : ipReputationScore;
        if (score <= 0.25d) return 0.2d;
        if (score <= 0.6d) return 0.4d;
        return 1.0d;
    }

    private double calculateBurstScore(ReviewUser user, Instant now) {
        long reviewsLastHour = reviewRepository.countByUserIdAndCreatedAtAfter(user.getId(), now.minus(Duration.ofHours(1)));
        return Math.max(0d, 1d - reviewsLastHour / 10d);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private double clamp01(double value) {
        if (value < 0) return 0;
        return Math.min(value, 1.0d);
    }
}
