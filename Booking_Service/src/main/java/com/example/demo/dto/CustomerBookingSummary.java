package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class CustomerBookingSummary {
    private Long bookingId;
    private Long hotelId;
    private Long roomTypeId;

    private LocalDate checkIn;
    private LocalDate checkOut;

    private int rooms;
    private int guestCount;

    private String status;
    private BigDecimal totalAmount;

    private Instant createdAt;
}