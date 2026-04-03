package com.example.demo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookingNotificationRequest {
    private Long bookingId;
    private Long hotelId;
    private String guestName;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String roomType;
    private BigDecimal totalPrice;
}
