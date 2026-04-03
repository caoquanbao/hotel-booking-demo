package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class NotificationRequest {
    private String type;
    private NotificationRecipient recipient;
    private Map<String, Object> payload;
    private NotificationMetadata metadata;
}
