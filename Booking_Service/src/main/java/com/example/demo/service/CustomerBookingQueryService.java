package com.example.demo.service;

import com.example.demo.dto.CustomerBookingSummary;
import com.example.demo.entity.Booking;
import com.example.demo.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerBookingQueryService {

    private final BookingRepository bookingRepository;

    public List<CustomerBookingSummary> listMyBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return bookings.stream().map(b ->
                CustomerBookingSummary.builder()
                        .bookingId(b.getId())
                        .hotelId(b.getHotelId())
                        .roomTypeId(b.getRoomTypeId())
                        .checkIn(b.getCheckIn())
                        .checkOut(b.getCheckOut())
                        .rooms(b.getRooms())
                        .guestCount(b.getGuestCount())
                        .status(b.getStatus().name())
                        .totalAmount(b.getTotalAmount())
                        .createdAt(b.getCreatedAt())
                        .build()
        ).toList();
    }
}