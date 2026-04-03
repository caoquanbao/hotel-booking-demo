package com.example.demo.pricing;

import lombok.Builder;

@Builder
public record PricingResult(
        long basePrice,
        long promotionDiscount,
        long tierDiscount,
        long finalPrice,
        long commissionAmount,
        long hotelPayout
) {
}
