package com.example.demo.controller;

import com.example.demo.dto.BidSuggestionResponse;
import com.example.demo.service.BidRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ads")
@RequiredArgsConstructor
public class BidRecommendationController {

    private final BidRecommendationService bidRecommendationService;

    @GetMapping("/bid-suggestion")
    public ResponseEntity<BidSuggestionResponse> suggestion(@RequestParam("city") String city) {
        return ResponseEntity.ok(bidRecommendationService.getBidSuggestion(city));
    }
}
