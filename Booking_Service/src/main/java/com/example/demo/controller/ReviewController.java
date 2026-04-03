package com.example.demo.controller;

import com.example.demo.dto.CreateReviewRequest;
import com.example.demo.dto.CreateReviewResponse;
import com.example.demo.service.CurrentUserService;
import com.example.demo.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<CreateReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request,
                                                             HttpServletRequest httpServletRequest) {
        String ipAddress = httpServletRequest.getRemoteAddr();
        CreateReviewResponse response = reviewService.saveReview(request, currentUserService.getCurrentUserId(), ipAddress);
        return ResponseEntity.ok(response);
    }
}
