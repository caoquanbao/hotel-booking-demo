package com.example.demo.client;

import com.example.demo.dto.BookingNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class RenterClient {

    private final RestClient restClient = RestClient.create();

    @Value("${renter.service.base-url:http://localhost:8082}")
    private String renterServiceBaseUrl;

    public void sendBookingNotification(BookingNotificationRequest request) {
        restClient.post()
                .uri(renterServiceBaseUrl + "/internal/renter/booking-notification")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
