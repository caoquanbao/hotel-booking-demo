package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_review_hotel_created", columnList = "hotelId,createdAt"),
        @Index(name = "idx_review_user_created", columnList = "userId,createdAt"),
        @Index(name = "idx_review_ip_created", columnList = "ipAddress,createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long hotelId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer starRating;

    @Column(nullable = false, length = 3000)
    private String reviewText;

    @Column(nullable = false)
    private Double trustScore;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false, length = 64)
    private String ipAddress;

    @Column(nullable = false)
    private Boolean verifiedStay;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
