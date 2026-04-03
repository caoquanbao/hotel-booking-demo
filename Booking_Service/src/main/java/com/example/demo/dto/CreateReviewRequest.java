package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReviewRequest {

    @NotNull
    private Long hotelId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer starRating;

    @NotBlank
    private String reviewText;
}
