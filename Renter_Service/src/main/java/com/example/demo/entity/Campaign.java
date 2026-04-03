package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "campaigns", indexes = {
        @Index(name = "idx_campaign_hotel", columnList = "hotelId"),
        @Index(name = "idx_campaign_status_time", columnList = "status,startTime,endTime")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long hotelId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal bidPrice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyBudget;

    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CampaignStatus status;
}
