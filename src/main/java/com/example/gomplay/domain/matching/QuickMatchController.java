package com.example.gomplay.domain.matching;

import com.example.gomplay.domain.matching.dto.MatchingToggleRequest;
import com.example.gomplay.domain.matching.dto.MatchingToggleResponse;
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
}
