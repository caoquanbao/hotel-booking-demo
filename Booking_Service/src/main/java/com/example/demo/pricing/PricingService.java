package com.example.demo.pricing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    private final DiscountEngine discountEngine;
    private final double commissionRate;

    public PricingService(DiscountEngine discountEngine,
                          @Value("${pricing.commission-rate:0.30}") double commissionRate) {
        this.discountEngine = discountEngine;
        this.commissionRate = commissionRate;
    }

    /**
     * Pricing order:
     * base -> promotion -> tier -> final -> commission(from base) -> hotel payout.
     */
    public PricingResult calculatePrice(long basePrice) {
        DiscountResult discount = discountEngine.calculateDiscount(basePrice);

        long commissionAmount = Math.round(discount.basePrice() * commissionRate);
        long hotelPayout = Math.max(0L, discount.basePrice() - commissionAmount);

        return PricingResult.builder()
                .basePrice(discount.basePrice())
                .promotionDiscount(discount.promotionDiscount())
                .tierDiscount(discount.tierDiscount())
                .finalPrice(discount.finalPrice())
                .commissionAmount(commissionAmount)
                .hotelPayout(hotelPayout)
                .build();
    }
}
