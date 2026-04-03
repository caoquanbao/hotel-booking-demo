package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RegisterClickRequest {
    @NotNull
    private Long campaignId;

    private Long userId;

    @NotNull
    private Long hotelId;

    @NotNull
    private BigDecimal price;
}
