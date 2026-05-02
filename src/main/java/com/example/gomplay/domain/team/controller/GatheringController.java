package com.example.gomplay.domain.team.controller;


import com.example.gomplay.domain.team.dto.GatheringUpdateRequest;
import com.example.gomplay.domain.team.dto.GatheringUpdateResponse;
import com.example.gomplay.domain.team.dto.GatheringCreateRequest;
import com.example.gomplay.domain.team.dto.GatheringCreateResponse;
import com.example.gomplay.domain.team.dto.GatheringJoinResponse;
import com.example.gomplay.domain.team.dto.GatheringDetailResponse;
import com.example.gomplay.domain.team.service.GatheringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.gomplay.domain.team.dto.GatheringListResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;


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

    @PatchMapping("/{id}")
    public ResponseEntity<GatheringUpdateResponse> updateGathering(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long id,
        @RequestBody GatheringUpdateRequest request) {
    return ResponseEntity.ok(gatheringService.updateGathering(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteGathering(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long id) {
    gatheringService.deleteGathering(userId, id);
    return ResponseEntity.ok("모집글이 삭제되었습니다.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<GatheringDetailResponse> getGathering(
        @PathVariable Long id) {
    return ResponseEntity.ok(gatheringService.getGathering(id));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<GatheringJoinResponse> joinGathering(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long id) {
    return ResponseEntity.ok(gatheringService.joinGathering(userId, id));
    }

    @GetMapping
    public ResponseEntity<Page<GatheringListResponse>> getGatheringList(
        @RequestParam(required = false) String sportType,
        @RequestParam(required = false) String difficulty,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(gatheringService.getGatheringList(sportType, difficulty, page, size));
    }
}