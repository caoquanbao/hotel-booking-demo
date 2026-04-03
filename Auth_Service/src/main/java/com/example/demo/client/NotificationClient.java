package com.example.demo.client;

import com.example.demo.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class NotificationClient {

    private final RestClient restClient = RestClient.create();

    @Value("${notification.service.base-url:http://localhost:3004}")
    private String notificationServiceBaseUrl;

    public void send(NotificationRequest request) {
        log.info("Sending {} notification to Notification_Service", request.getType());

        restClient.post()
                .uri(notificationServiceBaseUrl + "/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
