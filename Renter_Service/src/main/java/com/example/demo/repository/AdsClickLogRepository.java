package com.example.demo.repository;

import com.example.demo.entity.AdsClickLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface AdsClickLogRepository extends JpaRepository<AdsClickLog, Long> {
    long countByCampaignIdAndClickTimeAfter(Long campaignId, Instant fromTime);
}
