package com.example.demo.repository;

import com.example.demo.entity.Campaign;
import com.example.demo.entity.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByStatusAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            CampaignStatus status,
            Instant nowStart,
            Instant nowEnd
    );
}
