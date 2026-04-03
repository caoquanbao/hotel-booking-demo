package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationRecipient {
    private String email;
    private String name;
    private String telegramChatId;
}
