package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateBookingRequest {
    public enum PaymentMethod {
        MOMO, VNPAY
    }

    private Long hotelId;
    private Long roomTypeId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int rooms;
    private int guestCount;
    private PaymentMethod paymentMethod; // optional, default MOMO
    private String customerEmail;        // optional
}
