package com.example.gomplay.domain.matching;

import com.example.gomplay.domain.matching.dto.*;
import com.example.gomplay.domain.matching.service.QuickMatchService;
import com.example.gomplay.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match")
public class QuickMatchController {

    private final QuickMatchService quickMatchService;

    @PatchMapping("/toggle")
    public ResponseEntity<ApiResponse<MatchingToggleResponse>> updateMatchingStatus(
            @AuthenticationPrincipal Long userId,
            @RequestBody MatchingToggleRequest request) {
        MatchingToggleResponse response = quickMatchService.updateMatchingStatus(userId, request.isMatching());
        return ResponseEntity.ok(ApiResponse.success(
                response.getIsMatching() ? "매칭 대기 중입니다." : "매칭이 종료되었습니다.",
                response
        ));
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<MatchRequestResponse>> requestMatch(
            @AuthenticationPrincipal Long userId,
            @RequestBody MatchRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(
                "매칭 요청을 보냈습니다.",
                quickMatchService.requestMatch(userId, request)
        ));
    }

    @PatchMapping("/request/{id}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptMatch(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        quickMatchService.acceptMatch(userId, id);
        return ResponseEntity.ok(ApiResponse.success("매칭이 수락되었습니다.", null));
    }

    @PatchMapping("/request/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectMatch(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        quickMatchService.rejectMatch(userId, id);
        return ResponseEntity.ok(ApiResponse.success("매칭이 거절되었습니다.", null));
    }

    @PostMapping("/pass")
    public ResponseEntity<ApiResponse<CandidateResponse>> pass(
            @AuthenticationPrincipal Long userId,
            @RequestBody PassRequest request) {
        CandidateResponse next = quickMatchService.getNextCandidate(userId, request.getExcludeIds());
        if (next == null) {
            return ResponseEntity.ok(ApiResponse.success("추천할 상대가 없어요.", null));
        }
        return ResponseEntity.ok(ApiResponse.success("다음 추천입니다.", next));
    }
}