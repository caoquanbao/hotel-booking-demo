package com.example.demo.service;

import com.example.demo.dto.BookingNotificationRequest;
import com.example.demo.entity.OwnerNotification;
import com.example.demo.repository.OwnerNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String DEFAULT_MESSAGE = "You have a new booking. Payment completed.";

    private final OwnerNotificationRepository ownerNotificationRepository;

    @Transactional
    public void createOwnerNotification(BookingNotificationRequest request) {
        if (ownerNotificationRepository.existsByBookingId(request.getBookingId())) {
            return;
        }

        OwnerNotification notification = OwnerNotification.builder()
                .hotelId(request.getHotelId())
                .bookingId(request.getBookingId())
                .message(DEFAULT_MESSAGE)
                .isRead(false)
                .build();
        ownerNotificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<OwnerNotification> listNotifications(Long hotelId) {
        return ownerNotificationRepository.findByHotelIdOrderByCreatedAtDesc(hotelId);
    }
}
