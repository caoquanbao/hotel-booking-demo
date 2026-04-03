package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@Data
@Builder
public class CreateBookingResponse {
    private Long bookingId;
    private String status;
    private BigDecimal totalAmount;
    @JsonProperty("base_price")
    private Long basePrice;
    @JsonProperty("promotion_discount")
    private Long promotionDiscount;
    @JsonProperty("tier_discount")
    private Long tierDiscount;
    @JsonProperty("final_price")
    private Long finalPrice;
    private Long commission;
    @JsonProperty("hotel_payout")
    private Long hotelPayout;
    private String paymentUrl;
}
