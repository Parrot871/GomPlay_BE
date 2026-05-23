package com.example.gomplay.domain.matching;

import com.example.gomplay.domain.matching.dto.ActiveMatchResponse;
import com.example.gomplay.domain.matching.dto.MatchHistoryResponse;
import com.example.gomplay.domain.matching.service.ActiveMatchService;
import com.example.gomplay.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match")
public class MatchController {

    private final ActiveMatchService activeMatchService;

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ActiveMatchResponse>>> getActiveMatches(
            @AuthenticationPrincipal Long userId) {
        System.out.println("userId: " + userId);  // 추가
        return ResponseEntity.ok(ApiResponse.success(
                "활성 매칭 조회 성공",
                activeMatchService.getActiveMatches(userId)
        ));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<MatchHistoryResponse>>> getMatchHistory(
        @AuthenticationPrincipal Long userId) {
            return ResponseEntity.ok(ApiResponse.success(
            "매칭 히스토리 조회 성공",
            activeMatchService.getMatchHistory(userId)
        ));
    }
}