package com.example.demo.controller;

import com.example.demo.dto.RegisterClickRequest;
import com.example.demo.service.AdsClickService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ads/click")
@RequiredArgsConstructor
public class AdsClickController {

    private final AdsClickService adsClickService;

    @PostMapping
    public ResponseEntity<Void> registerClick(@Valid @RequestBody RegisterClickRequest request) {
        adsClickService.registerClick(request);
        return ResponseEntity.ok().build();
    }
}
