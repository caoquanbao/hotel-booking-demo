package com.example.demo.service;

import com.example.demo.entity.AdsAuctionStats;
import com.example.demo.entity.Campaign;
import com.example.demo.entity.CampaignStatus;
import com.example.demo.repository.AdsAuctionStatsRepository;
import com.example.demo.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdsAnalyticsService {

    private final CampaignRepository campaignRepository;
    private final AdsAuctionStatsRepository statsRepository;

    @Transactional
    public AdsAuctionStats aggregateStats(String city) {
        Instant now = Instant.now();
        List<Campaign> activeCampaigns = campaignRepository
                .findByStatusAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(CampaignStatus.ACTIVE, now, now);

        if (activeCampaigns.isEmpty()) {
            return statsRepository.save(AdsAuctionStats.builder()
                    .city(city)
                    .avgBid(BigDecimal.ZERO)
                    .p50Bid(BigDecimal.ZERO)
                    .p75Bid(BigDecimal.ZERO)
                    .p90Bid(BigDecimal.ZERO)
                    .avgCtr(0d)
                    .build());
        }

        List<BigDecimal> bids = activeCampaigns.stream()
                .map(Campaign::getBidPrice)
                .sorted(Comparator.naturalOrder())
                .toList();

        BigDecimal avg = bids.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(bids.size()), 2, RoundingMode.HALF_UP);

        AdsAuctionStats stats = AdsAuctionStats.builder()
                .city(city)
                .avgBid(avg)
                .p50Bid(percentile(bids, 0.5))
                .p75Bid(percentile(bids, 0.75))
                .p90Bid(percentile(bids, 0.90))
                .avgCtr(0.05d)
                .build();

        return statsRepository.save(stats);
    }

    private BigDecimal percentile(List<BigDecimal> sorted, double p) {
        if (sorted.isEmpty()) return BigDecimal.ZERO;
        int idx = (int) Math.ceil(p * sorted.size()) - 1;
        idx = Math.max(0, Math.min(idx, sorted.size() - 1));
        return sorted.get(idx);
    }
}
