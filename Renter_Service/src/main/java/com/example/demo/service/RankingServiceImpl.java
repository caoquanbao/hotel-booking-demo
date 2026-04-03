package com.example.demo.service;

import com.example.demo.config.RankingWeights;
import com.example.demo.dto.HotelCandidate;
import com.example.demo.dto.RankedHotel;
import com.example.demo.dto.SearchContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final RankingWeights weights;

    @Override
    public List<RankedHotel> rankHotels(List<HotelCandidate> candidates, SearchContext context) {
        validateInput(candidates, context);
        if (candidates.isEmpty()) {
            return List.of();
        }

        List<RankedHotel> ranked = new ArrayList<>(candidates.size());
        for (HotelCandidate candidate : candidates) {
            double score = computeOrganicScore(candidate, context);
            ranked.add(RankedHotel.builder()
                    .hotelId(candidate.getHotelId())
                    .score(score)
                    .build());
        }

        ranked.sort(Comparator.comparingDouble(RankedHotel::getScore).reversed());
        return ranked;
    }

    double computeOrganicScore(HotelCandidate candidate, SearchContext context) {
        double trust = normalizeTrustScore(candidate.getTrustScore());
        double popularity = computePopularityScore(candidate.getBookingCount());
        double distance = computeDistanceScore(candidate.getDistanceKm());
        double priceMatch = computePriceMatchScore(candidate.getPrice(), context.getUserBudget());

        return (trust * weights.getTrust())
                + (popularity * weights.getPopularity())
                + (distance * weights.getDistance())
                + (priceMatch * weights.getPriceMatch());
    }

    double normalizeTrustScore(double trustScore) {
        if (trustScore <= 0d) {
            return 0.5d;
        }
        return clamp01(trustScore / 5d);
    }

    double computePopularityScore(int bookingCount) {
        int safeCount = Math.max(bookingCount, 0);
        double score = Math.log10(safeCount + 1d) / 4d;
        return clamp01(score);
    }

    double computeDistanceScore(double distanceKm) {
        double safeDistance = Math.max(distanceKm, 0d);
        return clamp01(Math.exp(-0.3d * safeDistance));
    }

    double computePriceMatchScore(double hotelPrice, double userBudget) {
        if (userBudget <= 0d) {
            return 1d;
        }

        double diff = Math.abs(hotelPrice - userBudget);
        double sigma = userBudget * 0.2d;
        if (sigma <= 0d) {
            return 1d;
        }

        double exponent = -((diff * diff) / (2d * sigma * sigma));
        return clamp01(Math.exp(exponent));
    }

    private void validateInput(List<HotelCandidate> candidates, SearchContext context) {
        if (candidates == null) {
            throw new IllegalArgumentException("candidates cannot be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("context cannot be null");
        }
        for (HotelCandidate candidate : candidates) {
            if (candidate == null || candidate.getHotelId() == null) {
                throw new IllegalArgumentException("candidate and hotelId are required");
            }
        }
    }

    private double clamp01(double value) {
        if (value < 0d) {
            return 0d;
        }
        return Math.min(value, 1d);
    }
}
