package com.example.demo.repository;

import com.example.demo.entity.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {
    Optional<RequestLog> findByRequestId(String requestId);
}
