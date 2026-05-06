package com.example.gomplay.domain.matching;

import com.example.gomplay.domain.matching.dto.MatchingToggleRequest;
import com.example.gomplay.domain.matching.dto.MatchingToggleResponse;
import com.example.gomplay.domain.matching.service.QuickMatchService;
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
    public ResponseEntity<MatchingToggleResponse> updateMatchingStatus(
            @AuthenticationPrincipal Long userId,
            @RequestBody MatchingToggleRequest request) {
        return ResponseEntity.ok(quickMatchService.updateMatchingStatus(userId, request.isMatching()));
    }
}
