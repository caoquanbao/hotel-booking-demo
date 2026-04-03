package com.example.demo.service;

import com.example.demo.entity.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingNotificationService {

    private final JavaMailSender mailSender;

    @Value("${booking.mail.from:no-reply@booking.local}")
    private String fromMail;

    public void sendPaidEmail(Booking booking) {
        String to = booking.getCustomerEmail();
        if (to == null || to.isBlank()) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromMail);
        message.setTo(to);
        message.setSubject("Booking confirmed #" + booking.getId());
        message.setText("Your booking is confirmed and paid. Booking ID: " + booking.getId());
        mailSender.send(message);
    }
}
