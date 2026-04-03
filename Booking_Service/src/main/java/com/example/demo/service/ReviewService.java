package com.example.demo.service;

import com.example.demo.dto.CreateReviewRequest;
import com.example.demo.dto.CreateReviewResponse;
import com.example.demo.entity.Booking;
import com.example.demo.entity.HotelRatingSummary;
import com.example.demo.entity.Review;
import com.example.demo.entity.ReviewUser;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.ReviewValidationException;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.ReviewUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewUserRepository reviewUserRepository;
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final TrustScoreService trustScoreService;
    private final FraudDetectionService fraudDetectionService;
    private final RatingAggregatorService ratingAggregatorService;

    @Transactional
    public CreateReviewResponse saveReview(CreateReviewRequest request, Long userId, String ipAddress) {
        validateRequest(request);

        ReviewUser user = reviewUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        boolean verifiedStay = bookingRepository.existsByUserIdAndHotelIdAndStatusIn(
                userId,
                request.getHotelId(),
                Booking.successfulStatuses()
        );

        Instant now = Instant.now();
        double baseTrustScore = trustScoreService.calculateTrustScore(user, request.getReviewText(), now);
        double trustScore = fraudDetectionService.applySpamPenalties(
                baseTrustScore,
                user,
                request.getHotelId(),
                request.getReviewText(),
                ipAddress,
                now
        );
        double weight = trustScoreService.convertTrustToWeight(trustScore);

        Review review = Review.builder()
                .hotelId(request.getHotelId())
                .userId(userId)
                .starRating(request.getStarRating())
                .reviewText(request.getReviewText())
                .trustScore(trustScore)
                .weight(weight)
                .ipAddress(ipAddress)
                .verifiedStay(verifiedStay)
                .createdAt(now)
                .build();
        Review saved = reviewRepository.save(review);

        user.setReviewCount(user.getReviewCount() + 1);
        reviewUserRepository.save(user);

        HotelRatingSummary summary = ratingAggregatorService.updateHotelRating(
                request.getHotelId(),
                request.getStarRating(),
                weight
        );

        return CreateReviewResponse.builder()
                .reviewId(saved.getId())
                .hotelId(saved.getHotelId())
                .starRating(saved.getStarRating())
                .trustScore(saved.getTrustScore())
                .weight(saved.getWeight())
                .hotelRating(summary.getRating())
                .hotelReviewCount(summary.getReviewCount())
                .build();
    }

    private void validateRequest(CreateReviewRequest request) {
        if (request.getStarRating() == null || request.getStarRating() < 1 || request.getStarRating() > 5) {
            throw new ReviewValidationException("starRating must be between 1 and 5");
        }
        if (request.getReviewText() == null || request.getReviewText().isBlank()) {
            throw new ReviewValidationException("reviewText is required");
        }
    }
}
