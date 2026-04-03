package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AuctionResultItem {
    private Long campaignId;
    private Long hotelId;
    private double adRank;
    private BigDecimal finalPrice;
}
