package com.example.demo.dto;

public record CancelBookingResponse(
        Long bookingId,
        String status,
        boolean inventoryRestored
) {
}
