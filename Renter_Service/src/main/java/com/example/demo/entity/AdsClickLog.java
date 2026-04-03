package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ads_click_log", indexes = {
        @Index(name = "idx_ads_click_campaign_time", columnList = "campaignId,clickTime"),
        @Index(name = "idx_ads_click_hotel_time", columnList = "hotelId,clickTime")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdsClickLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long campaignId;

    private Long userId;

    @Column(nullable = false)
    private Long hotelId;

    @Column(nullable = false)
    private Instant clickTime;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal priceCharged;

    @PrePersist
    public void prePersist() {
        if (clickTime == null) {
            clickTime = Instant.now();
        }
    }
}
