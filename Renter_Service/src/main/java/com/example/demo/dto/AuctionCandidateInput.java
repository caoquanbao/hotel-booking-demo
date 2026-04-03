package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuctionCandidateInput {
    @NotNull
    private Long campaignId;

    // trust score is produced by Booking_Service (1..5)
    @NotNull
    private Double trustScore;
}
