package com.example.gomplay.domain.team.controller;


import com.example.gomplay.domain.team.dto.GatheringUpdateRequest;
import com.example.gomplay.domain.team.dto.GatheringUpdateResponse;
import com.example.gomplay.domain.team.dto.GatheringCreateRequest;
import com.example.gomplay.domain.team.dto.GatheringCreateResponse;
import com.example.gomplay.domain.team.dto.GatheringJoinResponse;
import com.example.gomplay.domain.team.dto.GatheringDetailResponse;
import com.example.gomplay.domain.team.service.GatheringRecommendService;
import com.example.gomplay.domain.team.service.GatheringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.gomplay.domain.team.dto.GatheringListResponse;
import com.example.gomplay.domain.team.dto.GatheringParticipantResponse;
import com.example.gomplay.domain.team.dto.GatheringRecommendResponse;
import java.util.List;

import org.springframework.data.domain.Page;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gathering")
public class GatheringController {

    private final GatheringService gatheringService;
    private final GatheringRecommendService gatheringRecommendService; 

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

    @PatchMapping("/{gatheringId}/participants/{participantId}/accept")
public ResponseEntity<GatheringParticipantResponse> acceptParticipant(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long gatheringId,
        @PathVariable Long participantId) {
    return ResponseEntity.ok(gatheringService.acceptParticipant(userId, gatheringId, participantId));
}

    @PatchMapping("/{gatheringId}/participants/{participantId}/reject")
public ResponseEntity<GatheringParticipantResponse> rejectParticipant(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long gatheringId,
        @PathVariable Long participantId) {
    return ResponseEntity.ok(gatheringService.rejectParticipant(userId, gatheringId, participantId));
}

    @GetMapping("/recommend")
    public ResponseEntity<List<GatheringRecommendResponse>> getRecommendedGatherings(
        @AuthenticationPrincipal Long userId) {
    return ResponseEntity.ok(gatheringRecommendService.getRecommendedGatherings(userId));
    } 


    @PatchMapping("/{gatheringId}/complete")
    public ResponseEntity<Void> completeGathering(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long gatheringId) {
    gatheringService.completeGathering(userId, gatheringId);
    return ResponseEntity.ok().build();
    }

}