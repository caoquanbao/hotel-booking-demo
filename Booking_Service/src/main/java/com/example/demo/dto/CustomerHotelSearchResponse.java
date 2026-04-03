package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CustomerHotelSearchResponse {
    private Long hotelId;
    private String hotelName;
    private String location;

    private Long roomTypeId;
    private String roomTypeName;

    private int roomsRequested;
    private boolean available;

    // optional preview price (stub)
    private BigDecimal estimatedTotal;
    private LocalDate checkIn;
    private LocalDate checkOut;
}