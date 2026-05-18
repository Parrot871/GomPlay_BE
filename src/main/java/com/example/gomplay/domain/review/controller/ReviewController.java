package com.example.gomplay.domain.review.controller;

import com.example.gomplay.domain.review.dto.ReviewRequest;
import com.example.gomplay.domain.review.dto.ReviewResponse;
import com.example.gomplay.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    // 평가 제출
    @PostMapping
    public ResponseEntity<ReviewResponse> submitReview(
            @AuthenticationPrincipal Long userId,
            @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.submitReview(userId, request));
    }

    // 평가 조회
    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getReviews(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(reviewService.getReviews(userId));
    }
}