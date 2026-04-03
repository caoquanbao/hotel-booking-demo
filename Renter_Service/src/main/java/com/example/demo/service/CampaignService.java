package com.example.demo.service;

import com.example.demo.dto.CampaignResponse;
import com.example.demo.dto.CreateCampaignRequest;
import com.example.demo.entity.Campaign;
import com.example.demo.entity.CampaignStatus;
import com.example.demo.entity.CampaignWallet;
import com.example.demo.exception.AdsAuctionException;
import com.example.demo.repository.CampaignRepository;
import com.example.demo.repository.CampaignWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignWalletRepository campaignWalletRepository;
    private final WalletService walletService;

    @Transactional
    public CampaignResponse createCampaign(CreateCampaignRequest request) {
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new AdsAuctionException("end_time must be after start_time");
        }

        Campaign campaign = Campaign.builder()
                .hotelId(request.getHotelId())
                .bidPrice(request.getBidPrice())
                .dailyBudget(request.getDailyBudget())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(CampaignStatus.ACTIVE)
                .build();
        campaign = campaignRepository.save(campaign);

        CampaignWallet wallet = CampaignWallet.builder()
                .campaignId(campaign.getId())
                .campaign(campaign)
                .remainingBudget(campaign.getDailyBudget())
                .isOutOfBudget(false)
                .build();
        campaignWalletRepository.save(wallet);

        walletService.warmupBudgetCache(campaign.getId(), campaign.getDailyBudget());
        return toResponse(campaign);
    }

    @Transactional
    public void pauseCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new AdsAuctionException("Campaign not found: " + campaignId));
        campaign.setStatus(CampaignStatus.PAUSED);
        campaignRepository.save(campaign);
    }

    @Transactional(readOnly = true)
    public List<CampaignResponse> listActiveCampaigns() {
        Instant now = Instant.now();
        return campaignRepository
                .findByStatusAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(CampaignStatus.ACTIVE, now, now)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CampaignResponse toResponse(Campaign campaign) {
        return CampaignResponse.builder()
                .id(campaign.getId())
                .hotelId(campaign.getHotelId())
                .bidPrice(campaign.getBidPrice())
                .dailyBudget(campaign.getDailyBudget())
                .status(campaign.getStatus())
                .startTime(campaign.getStartTime())
                .endTime(campaign.getEndTime())
                .build();
    }
}
