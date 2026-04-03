package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Read model mapped to bookings table owned by Booking_Service.
 * Renter_Service only reads from this table for payout calculation.
 */
@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRecord {

    @Id
    private Long id;

    private Long userId;
    private Long hotelId;
    private Long roomTypeId;

    private LocalDate checkIn;
    private LocalDate checkOut;

    private Integer rooms;
    private Integer guestCount;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private Instant paidAt;
    private Instant createdAt;

    public enum BookingStatus {
        PENDING_PAYMENT,
        CONFIRMED,
        PAID,
        CANCELLED,
        COMPLETED
    }
}
