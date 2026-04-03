package com.example.demo.service;

import com.example.demo.entity.HotelRatingSummary;
import com.example.demo.repository.HotelRatingSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingAggregatorService {

    private final HotelRatingSummaryRepository summaryRepository;

    @Transactional
    public HotelRatingSummary updateHotelRating(Long hotelId, int star, double weight) {
        HotelRatingSummary summary = summaryRepository.findById(hotelId)
                .orElseGet(() -> HotelRatingSummary.builder()
                        .hotelId(hotelId)
                        .weightedSum(0d)
                        .weightSum(0d)
                        .rating(0d)
                        .reviewCount(0)
                        .build());

        double weightedSum = summary.getWeightedSum() + (star * weight);
        double weightSum = summary.getWeightSum() + weight;
        double rating = weightSum == 0 ? 0d : weightedSum / weightSum;

        summary.setWeightedSum(weightedSum);
        summary.setWeightSum(weightSum);
        summary.setRating(rating);
        summary.setReviewCount(summary.getReviewCount() + 1);

        return summaryRepository.save(summary);
    }
}
