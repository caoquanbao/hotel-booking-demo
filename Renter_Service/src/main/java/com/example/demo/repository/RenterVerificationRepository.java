package com.example.demo.repository;

import com.example.demo.entity.RenterVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RenterVerificationRepository extends JpaRepository<RenterVerification, Long> {
    Optional<RenterVerification> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
