package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "security_events",
       indexes = {
           @Index(name = "idx_security_user", columnList = "userId"),
           @Index(name = "idx_security_created", columnList = "createdAt")
       })
@Getter
@Setter
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // không bắt buộc foreign key để tránh fail nếu user bị xóa
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SecurityEventType eventType;

    private String ip;

    private String userAgent;

    @Column(length = 1000)
    private String metadata;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}