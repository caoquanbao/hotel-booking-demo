package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class CreateCampaignRequest {
    @NotNull
    private Long hotelId;

    @NotNull
    @DecimalMin("0")
    private BigDecimal bidPrice;

    @NotNull
    @DecimalMin("0")
    private BigDecimal dailyBudget;

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;
}
