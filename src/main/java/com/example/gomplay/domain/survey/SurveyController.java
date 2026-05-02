package com.example.gomplay.domain.survey;

import com.example.gomplay.domain.survey.dto.SurveyRequest;
import com.example.gomplay.domain.survey.dto.SurveyResponse;
import com.example.gomplay.domain.survey.service.SurveyService;
import com.example.gomplay.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/survey")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @PostMapping
    public ResponseEntity<ApiResponse<SurveyResponse>> saveSurvey(
            @AuthenticationPrincipal Long userId,
            @RequestBody SurveyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("설문이 저장되었습니다.", surveyService.saveSurvey(userId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<SurveyResponse>> getSurvey(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success("설문 조회 성공", surveyService.getSurvey(userId)));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<SurveyResponse>> updateSurvey(
            @AuthenticationPrincipal Long userId,
            @RequestBody SurveyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("설문이 수정되었습니다.", surveyService.updateSurvey(userId, request)));
    }
}
