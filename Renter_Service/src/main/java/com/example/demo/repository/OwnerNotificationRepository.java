package com.example.demo.repository;

import com.example.demo.entity.OwnerNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OwnerNotificationRepository extends JpaRepository<OwnerNotification, Long> {
    boolean existsByBookingId(Long bookingId);
    List<OwnerNotification> findByHotelIdOrderByCreatedAtDesc(Long hotelId);
}
