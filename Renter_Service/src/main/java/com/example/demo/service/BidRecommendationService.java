package com.example.demo.service;

import com.example.demo.dto.BidSuggestionResponse;
import com.example.demo.entity.AdsAuctionStats;
import com.example.demo.repository.AdsAuctionStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class BidRecommendationService {

    private final AdsAuctionStatsRepository statsRepository;

    public BidSuggestionResponse getBidSuggestion(String city) {
        AdsAuctionStats stats = statsRepository.findById(city)
                .orElse(AdsAuctionStats.builder()
                        .city(city)
                        .avgBid(BigDecimal.valueOf(3500))
                        .p50Bid(BigDecimal.valueOf(3200))
                        .p75Bid(BigDecimal.valueOf(4600))
                        .p90Bid(BigDecimal.valueOf(7400))
                        .avgCtr(0.058)
                        .build());

        String avgRange = range(stats.getAvgBid(), 0.91, 1.06);
        String top3Range = range(stats.getP75Bid(), 1.00, 1.05);
        String top1Range = range(stats.getP90Bid(), 0.95, 1.07);

        int impressions = 1200;
        int clicks = Math.max(1, (int) Math.round(impressions * stats.getAvgCtr()));

        return BidSuggestionResponse.builder()
                .avgBid(avgRange)
                .recommendedTop3(top3Range)
                .recommendedTop1(top1Range)
                .estimatedImpressions(impressions)
                .estimatedClicks(clicks)
                .build();
    }

    private String range(BigDecimal base, double low, double high) {
        BigDecimal min = base.multiply(BigDecimal.valueOf(low)).setScale(0, RoundingMode.HALF_UP);
        BigDecimal max = base.multiply(BigDecimal.valueOf(high)).setScale(0, RoundingMode.HALF_UP);
        return min.toPlainString() + "-" + max.toPlainString();
    }
}
