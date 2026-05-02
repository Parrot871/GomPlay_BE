package com.example.gomplay.domain.survey;

import com.example.gomplay.domain.survey.dto.ScheduleRequest;
import com.example.gomplay.domain.survey.dto.ScheduleResponse;
import com.example.gomplay.domain.survey.service.ScheduleService;
import com.example.gomplay.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleResponse>> saveSchedule(
            @AuthenticationPrincipal Long userId,
            @RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("시간표가 저장되었습니다.", scheduleService.saveSchedule(userId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ScheduleResponse>> getSchedule(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success("시간표 조회 성공", scheduleService.getSchedule(userId)));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<ScheduleResponse>> updateSchedule(
            @AuthenticationPrincipal Long userId,
            @RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("시간표가 수정되었습니다.", scheduleService.updateSchedule(userId, request)));
    }
}