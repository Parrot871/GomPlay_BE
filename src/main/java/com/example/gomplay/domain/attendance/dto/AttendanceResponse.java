package com.example.gomplay.domain.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class AttendanceResponse {
    private LocalDate date;
    private int totalPoints;
    private int totalAttendance;
}