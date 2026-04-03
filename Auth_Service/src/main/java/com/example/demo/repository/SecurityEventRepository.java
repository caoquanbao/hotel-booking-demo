package com.example.demo.repository;

import com.example.demo.entity.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityEventRepository
        extends JpaRepository<SecurityEvent, Long> {
}