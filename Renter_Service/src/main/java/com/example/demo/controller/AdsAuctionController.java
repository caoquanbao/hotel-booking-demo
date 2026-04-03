package com.example.demo.controller;

import com.example.demo.dto.AuctionResultItem;
import com.example.demo.dto.RunAuctionRequest;
import com.example.demo.service.AdsAuctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ads/auction")
@RequiredArgsConstructor
public class AdsAuctionController {

    private final AdsAuctionService adsAuctionService;

    @PostMapping("/run")
    public ResponseEntity<List<AuctionResultItem>> run(@Valid @RequestBody RunAuctionRequest request) {
        List<AuctionResultItem> result = adsAuctionService.runAuction(
                request.getCandidateCampaigns(),
                request.getCity(),
                request.getTopN()
        );
        return ResponseEntity.ok(result);
    }
}
