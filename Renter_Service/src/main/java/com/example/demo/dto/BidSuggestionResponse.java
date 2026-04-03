package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BidSuggestionResponse {
    private String avgBid;
    private String recommendedTop3;
    private String recommendedTop1;
    private Integer estimatedImpressions;
    private Integer estimatedClicks;
}
