package com.example.gomplay.domain.attendance.controller;

import com.example.gomplay.domain.attendance.dto.AttendanceCalendarResponse;
import com.example.gomplay.domain.attendance.dto.AttendanceResponse;
import com.example.gomplay.domain.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<AttendanceResponse> checkAttendance(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(attendanceService.checkAttendance(userId));
    }

    @GetMapping("/calendar")
    public ResponseEntity<AttendanceCalendarResponse> getMonthlyAttendance(
            @AuthenticationPrincipal Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(attendanceService.getMonthlyAttendance(userId, year, month));
    }
}