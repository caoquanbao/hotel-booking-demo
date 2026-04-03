package com.example.demo.controller;

import com.example.demo.dto.BookingNotificationRequest;
import com.example.demo.entity.OwnerNotification;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RenterNotificationController {

    private final NotificationService notificationService;

    @PostMapping("/internal/renter/booking-notification")
    public ResponseEntity<Void> receiveBookingNotification(@RequestBody BookingNotificationRequest request) {
        notificationService.createOwnerNotification(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/renter/notifications")
    public ResponseEntity<List<OwnerNotification>> listNotifications(@RequestParam("hotelId") Long hotelId) {
        return ResponseEntity.ok(notificationService.listNotifications(hotelId));
    }
}
