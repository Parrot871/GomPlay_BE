package com.example.gomplay.domain.matching;

import com.example.gomplay.domain.matching.dto.*;
import com.example.gomplay.domain.matching.service.QuickMatchService;
import com.example.gomplay.domain.survey.repository.UserSurveyExerciseRepository;
import com.example.gomplay.domain.survey.repository.UserSurveyRepository;
import com.example.gomplay.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match")
public class QuickMatchController {

    private final QuickMatchService quickMatchService;
    private final UserSurveyRepository userSurveyRepository;
    private final UserSurveyExerciseRepository userSurveyExerciseRepository;

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

    @GetMapping("/candidates")
    public ResponseEntity<ApiResponse<List<CandidateResponse>>> getCandidates(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "매칭 후보 목록입니다.",
                quickMatchService.getCandidates(userId)
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
}
