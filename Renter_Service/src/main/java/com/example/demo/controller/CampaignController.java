package com.example.demo.controller;

import com.example.demo.dto.CampaignResponse;
import com.example.demo.dto.CreateCampaignRequest;
import com.example.demo.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ads/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    public ResponseEntity<CampaignResponse> create(@Valid @RequestBody CreateCampaignRequest request) {
        return ResponseEntity.ok(campaignService.createCampaign(request));
    }

    @PostMapping("/{campaignId}/pause")
    public ResponseEntity<Void> pause(@PathVariable Long campaignId) {
        campaignService.pauseCampaign(campaignId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<CampaignResponse>> active() {
        return ResponseEntity.ok(campaignService.listActiveCampaigns());
    }
}
