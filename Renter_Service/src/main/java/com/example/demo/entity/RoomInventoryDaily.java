package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "room_inventory_daily",
        uniqueConstraints = @UniqueConstraint(name = "uk_inventory_daily_hotel_room_rate_date",
                columnNames = {"hotelId", "roomTypeId", "ratePlanId", "stayDate"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomInventoryDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String hotelId;

    @Column(nullable = false, length = 64)
    private String roomTypeId;

    @Column(nullable = false, length = 64)
    private String ratePlanId;

    @Column(nullable = false)
    private LocalDate stayDate;

    @Column(nullable = false)
    private Integer totalInventory;

    @Column(nullable = false)
    @Builder.Default
    private Integer soldInventory = 0;

    @Column(nullable = false, length = 32)
    private String status;

    private Integer minStay;
    private Integer maxStay;

    @Column(nullable = false)
    @Builder.Default
    private Boolean closedToArrival = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean closedToDeparture = false;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (soldInventory == null) {
            soldInventory = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
