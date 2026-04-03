package com.example.demo.pricing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DiscountEngine {

    private final DiscountService discountService;

    public DiscountResult calculateDiscount(long basePrice) {
        long safeBasePrice = Math.max(basePrice, 0L);

        long promotionDiscount = calculatePromotionDiscount(safeBasePrice);
        long priceAfterPromotion = Math.max(0L, safeBasePrice - promotionDiscount);

        long tierDiscount = calculateTierDiscount(priceAfterPromotion);
        long finalPrice = Math.max(0L, priceAfterPromotion - tierDiscount);

        return DiscountResult.builder()
                .basePrice(safeBasePrice)
                .promotionDiscount(promotionDiscount)
                .tierDiscount(tierDiscount)
                .finalPrice(finalPrice)
                .build();
    }

    private long calculatePromotionDiscount(long basePrice) {
        Optional<Promotion> activePromotion = discountService.getActivePromotion(Instant.now());
        if (activePromotion.isEmpty()) {
            return 0L;
        }

        Promotion promotion = activePromotion.get();
        return switch (promotion.discountType()) {
            case PERCENT -> (basePrice * promotion.discountValue()) / 100L;
            case FIXED -> Math.min(basePrice, promotion.discountValue());
        };
    }

    private long calculateTierDiscount(long amountAfterPromotion) {
        OrderDiscountTier bestTier = null;
        for (OrderDiscountTier tier : discountService.getOrderDiscountTiers()) {
            if (amountAfterPromotion >= tier.minOrder()) {
                bestTier = tier;
            }
        }

        if (bestTier == null) {
            return 0L;
        }

        return switch (bestTier.discountType()) {
            case FIXED -> Math.min(amountAfterPromotion, bestTier.discountValue());
            case PERCENT -> (amountAfterPromotion * bestTier.discountValue()) / 100L;
        };
    }
}
