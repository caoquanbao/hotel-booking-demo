package com.example.demo.pricing;

import lombok.Builder;

@Builder
public record DiscountResult(
        long basePrice,
        long promotionDiscount,
        long tierDiscount,
        long finalPrice
) {
}
