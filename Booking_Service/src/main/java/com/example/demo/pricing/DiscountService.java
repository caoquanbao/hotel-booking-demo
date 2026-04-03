package com.example.demo.pricing;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class DiscountService {

    // In-memory defaults; can be replaced by DB-backed repositories later.
    public Optional<Promotion> getActivePromotion(Instant now) {
        Promotion lowSeason = Promotion.builder()
                .discountType(Promotion.DiscountType.PERCENT)
                .discountValue(10L)
                .startTime(Instant.parse("2025-01-01T00:00:00Z"))
                .endTime(Instant.parse("2027-12-31T23:59:59Z"))
                .build();
        return lowSeason.isActive(now) ? Optional.of(lowSeason) : Optional.empty();
    }

    public List<OrderDiscountTier> getOrderDiscountTiers() {
        return List.of(
                OrderDiscountTier.builder().minOrder(5_000_000L).discountType(OrderDiscountTier.DiscountType.FIXED).discountValue(200_000L).build(),
                OrderDiscountTier.builder().minOrder(10_000_000L).discountType(OrderDiscountTier.DiscountType.FIXED).discountValue(500_000L).build(),
                OrderDiscountTier.builder().minOrder(15_000_000L).discountType(OrderDiscountTier.DiscountType.FIXED).discountValue(1_000_000L).build(),
                OrderDiscountTier.builder().minOrder(20_000_000L).discountType(OrderDiscountTier.DiscountType.FIXED).discountValue(1_500_000L).build(),
                OrderDiscountTier.builder().minOrder(30_000_000L).discountType(OrderDiscountTier.DiscountType.FIXED).discountValue(3_000_000L).build(),
                OrderDiscountTier.builder().minOrder(50_000_000L).discountType(OrderDiscountTier.DiscountType.PERCENT).discountValue(10L).build()
        ).stream().sorted(Comparator.comparingLong(OrderDiscountTier::minOrder)).toList();
    }
}
