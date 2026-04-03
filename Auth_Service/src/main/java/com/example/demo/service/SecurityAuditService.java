package com.example.demo.service;

import com.example.demo.entity.SecurityEvent;
import com.example.demo.entity.SecurityEventType;
import com.example.demo.repository.SecurityEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityAuditService {

    private final SecurityEventRepository repository;

    public void log(Long userId,
                    SecurityEventType type,
                    String ip,
                    String userAgent,
                    String metadata) {

        try {
            SecurityEvent event = new SecurityEvent();
            event.setUserId(userId);
            event.setEventType(type);
            event.setIp(ip);
            event.setUserAgent(userAgent);
            event.setMetadata(metadata);

            repository.save(event);
        } catch (Exception e) {
            log.error("Failed to persist security audit event type {} for user {}: {}",
                    type, userId, e.getMessage(), e);
        }
    }
}
