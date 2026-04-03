package com.example.demo.service;

import com.example.demo.dto.AuctionCandidateInput;
import com.example.demo.dto.AuctionResultItem;
import com.example.demo.entity.AdsAuctionStats;
import com.example.demo.entity.Campaign;
import com.example.demo.entity.CampaignStatus;
import com.example.demo.exception.AdsAuctionException;
import com.example.demo.repository.AdsAuctionStatsRepository;
import com.example.demo.repository.CampaignRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdsAuctionService {

    private static final double K = 0.002d;
    private static final double EPSILON = 0.01d;
    private static final BigDecimal FLOOR_PRICE = BigDecimal.valueOf(1000);

    private final CampaignRepository campaignRepository;
    private final AdsAuctionStatsRepository statsRepository;
    private final WalletService walletService;

    public List<AuctionResultItem> runAuction(List<AuctionCandidateInput> candidateCampaigns, String city, Integer topN) {
        try {
            return CompletableFuture.supplyAsync(() -> runAuctionInternal(candidateCampaigns, city, topN))
                    .get(50, TimeUnit.MILLISECONDS);
        } catch (Exception timeoutOrError) {
            log.error("Auction execution failed for city {}: {}", city, timeoutOrError.getMessage(), timeoutOrError);
            throw new AdsAuctionException("Auction execution failed: " + timeoutOrError.getMessage(), timeoutOrError);
        }
    }

    private List<AuctionResultItem> runAuctionInternal(List<AuctionCandidateInput> candidateCampaigns,
                                                       String city,
                                                       Integer topN) {
        if (candidateCampaigns == null || candidateCampaigns.isEmpty()) {
            return List.of();
        }

        int safeTopN = (topN == null || topN <= 0) ? 3 : topN;
        Instant now = Instant.now();

        Map<Long, Campaign> campaignMap = campaignRepository.findAllById(
                        candidateCampaigns.stream().map(AuctionCandidateInput::getCampaignId).toList())
                .stream().collect(Collectors.toMap(Campaign::getId, c -> c));

        double medianBid = statsRepository.findById(city)
                .map(AdsAuctionStats::getP50Bid)
                .map(BigDecimal::doubleValue)
                .orElse(3000d);

        List<ScoredAd> scoredAds = candidateCampaigns.parallelStream()
                .map(input -> toScoredAd(input, campaignMap.get(input.getCampaignId()), medianBid, now))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(ScoredAd::getAdRank).reversed())
                .toList();

        // Bonus: diversity rule - maximum one ad per hotel.
        List<ScoredAd> diversified = new ArrayList<>();
        Set<Long> usedHotels = new HashSet<>();
        for (ScoredAd ad : scoredAds) {
            if (usedHotels.add(ad.getHotelId())) {
                diversified.add(ad);
            }
            if (diversified.size() >= safeTopN) {
                break;
            }
        }

        List<AuctionResultItem> result = new ArrayList<>();
        for (int i = 0; i < diversified.size(); i++) {
            ScoredAd winner = diversified.get(i);
            double nextAdRank = (i + 1 < diversified.size()) ? diversified.get(i + 1).getAdRank() : 0d;

            double calculatedPrice = (nextAdRank / Math.max(winner.getNormQuality(), 0.1d)) + EPSILON;
            BigDecimal finalPrice = BigDecimal.valueOf(calculatedPrice).setScale(2, RoundingMode.HALF_UP);
            if (finalPrice.compareTo(FLOOR_PRICE) < 0) {
                finalPrice = FLOOR_PRICE;
            }

            if (!walletService.checkBudget(winner.getCampaignId(), finalPrice)) {
                continue;
            }

            result.add(AuctionResultItem.builder()
                    .campaignId(winner.getCampaignId())
                    .hotelId(winner.getHotelId())
                    .adRank(winner.getAdRank())
                    .finalPrice(finalPrice)
                    .build());
        }

        return result;
    }

    private ScoredAd toScoredAd(AuctionCandidateInput input,
                                Campaign campaign,
                                double medianBid,
                                Instant now) {
        if (campaign == null) {
            return null;
        }
        if (campaign.getStatus() != CampaignStatus.ACTIVE) {
            return null;
        }
        if (campaign.getStartTime().isAfter(now) || campaign.getEndTime().isBefore(now)) {
            return null;
        }

        double normBid = 1d / (1d + Math.exp(-K * (campaign.getBidPrice().doubleValue() - medianBid)));
        double normQuality = clamp(input.getTrustScore() / 5d, 0.1d, 1d);
        double adRank = normBid * normQuality;

        return ScoredAd.builder()
                .campaignId(campaign.getId())
                .hotelId(campaign.getHotelId())
                .normQuality(normQuality)
                .adRank(adRank)
                .build();
    }

    private double clamp(double value, double min, double max) {
        if (value < min) return min;
        return Math.min(value, max);
    }

    @Value
    @Builder
    private static class ScoredAd {
        Long campaignId;
        Long hotelId;
        double normQuality;
        double adRank;
    }
}
