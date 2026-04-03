package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewUser {

    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private Instant accountCreatedAt;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalBookings = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer verifiedStays = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer helpfulVotesReceived = 0;

    @Column(nullable = false)
    @Builder.Default
    private Double cancelRate = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Double ipReputationScore = 1.0;
}
