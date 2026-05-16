package com.example.gomplay.domain.point.controller;

import com.example.gomplay.domain.point.dto.PointLogResponse;
import com.example.gomplay.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/point")
public class PointController {

    private final PointService pointService;

    @GetMapping("/logs")
    public ResponseEntity<List<PointLogResponse>> getPointLogs(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(pointService.getPointLogs(userId));
    }
}