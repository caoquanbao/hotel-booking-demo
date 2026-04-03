package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateReviewResponse {
    private Long reviewId;
    private Long hotelId;
    private Integer starRating;
    private Double trustScore;
    private Double weight;
    private Double hotelRating;
    private Integer hotelReviewCount;
}
