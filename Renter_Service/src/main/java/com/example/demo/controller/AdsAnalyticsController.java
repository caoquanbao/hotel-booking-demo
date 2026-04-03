package com.example.demo.controller;

import com.example.demo.entity.AdsAuctionStats;
import com.example.demo.service.AdsAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ads/analytics")
@RequiredArgsConstructor
public class AdsAnalyticsController {

    private final AdsAnalyticsService adsAnalyticsService;

    @PostMapping("/aggregate")
    public ResponseEntity<AdsAuctionStats> aggregate(@RequestParam("city") String city) {
        return ResponseEntity.ok(adsAnalyticsService.aggregateStats(city));
    }
}
