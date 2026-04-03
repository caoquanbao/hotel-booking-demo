package com.example.demo.repository;

import com.example.demo.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Booking> findByIdempotencyKey(String idempotencyKey);
    Optional<Booking> findByIdAndUserId(Long id, Long userId);
    Optional<Booking> findByPaymentOrderId(String paymentOrderId);
    boolean existsByUserIdAndHotelIdAndStatusIn(Long userId, Long hotelId, Set<Booking.BookingStatus> statuses);
    long countByHotelIdAndStatusIn(Long hotelId, Set<Booking.BookingStatus> statuses);
}
