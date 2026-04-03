package com.example.demo.service;

import com.example.demo.dto.RegisterClickRequest;
import com.example.demo.entity.AdsClickLog;
import com.example.demo.exception.AdsAuctionException;
import com.example.demo.repository.AdsClickLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AdsClickService {

    private final WalletService walletService;
    private final AdsClickLogRepository clickLogRepository;

    @Transactional
    public void registerClick(RegisterClickRequest request) {
        boolean deducted = walletService.deductBudget(request.getCampaignId(), request.getPrice());
        if (!deducted) {
            throw new AdsAuctionException("Campaign budget is insufficient");
        }

        AdsClickLog log = AdsClickLog.builder()
                .campaignId(request.getCampaignId())
                .userId(request.getUserId())
                .hotelId(request.getHotelId())
                .clickTime(Instant.now())
                .priceCharged(request.getPrice())
                .build();
        clickLogRepository.save(log);
    }
}
