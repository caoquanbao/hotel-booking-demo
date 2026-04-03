package com.example.demo.dto;

import com.example.demo.entity.CampaignStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class CampaignResponse {
    private Long id;
    private Long hotelId;
    private BigDecimal bidPrice;
    private BigDecimal dailyBudget;
    private CampaignStatus status;
    private Instant startTime;
    private Instant endTime;
}
