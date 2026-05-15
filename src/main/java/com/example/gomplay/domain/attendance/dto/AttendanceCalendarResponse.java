package com.example.gomplay.domain.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class AttendanceCalendarResponse {
    private int year;
    private int month;
    private List<LocalDate> attendanceDates;
    private int monthlyCount;
}