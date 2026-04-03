package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelCandidate {
    private Long hotelId;
    private double trustScore;
    private double distanceKm;
    private double price;
    private int bookingCount;
}
