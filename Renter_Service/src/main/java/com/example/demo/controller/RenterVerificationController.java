package com.example.demo.controller;

import com.example.demo.dto.RenterVerificationRequest;
import com.example.demo.dto.VerificationResult;
import com.example.demo.service.RenterVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/renter")
@RequiredArgsConstructor
public class RenterVerificationController {

    private final RenterVerificationService renterVerificationService;

    @PostMapping("/verify")
    public ResponseEntity<VerificationResult> verify(@Valid @RequestBody RenterVerificationRequest request) {
        return ResponseEntity.ok(renterVerificationService.verifyRenter(request));
    }
}
