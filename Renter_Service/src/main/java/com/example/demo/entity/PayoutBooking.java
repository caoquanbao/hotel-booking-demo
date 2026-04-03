package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payout_bookings", indexes = {
        @Index(name = "idx_payout_booking_payout", columnList = "payoutId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayoutBooking {

    /**
     * bookingId is globally unique to guarantee idempotency:
     * one booking can only be paid out once.
     */
    @Id
    private Long bookingId;

    @Column(nullable = false)
    private Long payoutId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal bookingAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal commissionAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal hotelNetAmount;
}
