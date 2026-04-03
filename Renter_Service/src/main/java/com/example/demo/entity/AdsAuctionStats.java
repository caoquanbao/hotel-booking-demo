package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ads_auction_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdsAuctionStats {

    @Id
    @Column(length = 128)
    private String city;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal avgBid;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal p50Bid;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal p75Bid;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal p90Bid;

    @Column(nullable = false)
    private Double avgCtr;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    public void touch() {
        updatedAt = Instant.now();
    }
}
