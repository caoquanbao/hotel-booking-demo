package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerHotelSearchRequest {
    private String location;     // city/area
    private LocalDate checkIn;
    private LocalDate checkOut;  // exclusive
    private Integer rooms;       // optional, default 1
    private Integer guestCount;  // optional
    private Double userBudget;   // optional, for organic ranking
    private Double userSearchLat; // optional
    private Double userSearchLng; // optional
}
