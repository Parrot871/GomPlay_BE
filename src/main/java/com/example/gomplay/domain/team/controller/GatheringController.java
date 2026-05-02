package com.example.gomplay.domain.team.controller;

import com.example.gomplay.domain.team.dto.GatheringCreateRequest;
import com.example.gomplay.domain.team.dto.GatheringCreateResponse;
import com.example.gomplay.domain.team.service.GatheringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gathering")
public class GatheringController {

    private final GatheringService gatheringService;

    @PostMapping
    public ResponseEntity<GatheringCreateResponse> createGathering(
            @AuthenticationPrincipal Long userId,
            @RequestBody GatheringCreateRequest request) {
        return ResponseEntity.ok(gatheringService.createGathering(userId, request));
    }
}