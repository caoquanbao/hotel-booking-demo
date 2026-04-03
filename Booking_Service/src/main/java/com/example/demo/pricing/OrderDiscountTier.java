package com.example.demo.pricing;

import lombok.Builder;

@Builder
public record OrderDiscountTier(
        long minOrder,
        DiscountType discountType,
        long discountValue
) {
    public enum DiscountType {
        FIXED,
        PERCENT
    }
}
