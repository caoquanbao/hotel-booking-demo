package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long hotelId;
    private Long roomTypeId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int rooms;
    private int guestCount;
    private BigDecimal totalAmount;
    private BigDecimal basePrice;
    private BigDecimal promotionDiscount;
    private BigDecimal tierDiscount;
    private BigDecimal finalPrice;
    private BigDecimal commissionAmount;
    private BigDecimal hotelPayout;
    
    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    private String idempotencyKey;
    private String paymentProvider;
    @Column(unique = true)
    private String paymentOrderId;
    private Instant paidAt;
    private String customerEmail;
    private Instant createdAt;

    public enum BookingStatus { PENDING_PAYMENT, CONFIRMED, PAID, CANCELLED, COMPLETED }

    // CONFIRMED is the current post-payment status; keep PAID for legacy rows.
    public static Set<BookingStatus> successfulStatuses() {
        return EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.PAID, BookingStatus.COMPLETED);
    }

    public static Set<BookingStatus> cancellableStatuses() {
        return EnumSet.of(BookingStatus.PENDING_PAYMENT, BookingStatus.CONFIRMED, BookingStatus.PAID);
    }
}
