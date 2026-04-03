package com.example.demo.pricing;

import lombok.Builder;

import java.time.Instant;

@Builder
public record Promotion(
        DiscountType discountType,
        long discountValue,
        Instant startTime,
        Instant endTime
) {
    public enum DiscountType {
        PERCENT,
        FIXED
    }

    public boolean isActive(Instant now) {
        return (startTime == null || !now.isBefore(startTime))
                && (endTime == null || !now.isAfter(endTime));
    }
}
