package com.example.gomplay.domain.attendance.service;

import com.example.gomplay.domain.attendance.dto.AttendanceCalendarResponse;
import com.example.gomplay.domain.attendance.dto.AttendanceResponse;
import com.example.gomplay.domain.attendance.entity.Attendance;
import com.example.gomplay.domain.attendance.repository.AttendanceRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserProfileRepository userProfileRepository;

    private static final int ATTENDANCE_POINT = 10;

    @Transactional
    public AttendanceResponse checkAttendance(Long userId) {
        UserProfile user = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        LocalDate today = LocalDate.now();

        if (attendanceRepository.existsByUserProfile_IdAndDate(user.getId(), today)) {
            throw new IllegalArgumentException("오늘 이미 출석했습니다.");
        }

        // 출석 저장
        Attendance attendance = Attendance.builder()
                .userProfile(user)
                .date(today)
                .build();
        attendanceRepository.save(attendance);

        // 포인트 적립
        user.addPoint(ATTENDANCE_POINT);

        int totalAttendance = attendanceRepository.countByUserProfile_Id(user.getId());

        return new AttendanceResponse(today, user.getPointBalance(), totalAttendance);
    }

    @Transactional(readOnly = true)
    public AttendanceCalendarResponse getMonthlyAttendance(Long userId, int year, int month) {
        UserProfile user = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Attendance> attendances = attendanceRepository
                .findByUserProfile_IdAndDateBetween(user.getId(), start, end);

        List<LocalDate> dates = attendances.stream()
                .map(Attendance::getDate)
                .collect(Collectors.toList());

        return new AttendanceCalendarResponse(year, month, dates, dates.size());
    }
}