package com.example.demo.repository;

import com.example.demo.entity.PayoutBooking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoutBookingRepository extends JpaRepository<PayoutBooking, Long> {
    boolean existsByBookingId(Long bookingId);
}
