package com.example.gomplay.domain.survey.service;

import com.example.gomplay.domain.survey.dto.ScheduleRequest;
import com.example.gomplay.domain.survey.dto.ScheduleResponse;
import com.example.gomplay.domain.survey.entity.UserSchedule;
import com.example.gomplay.domain.survey.repository.UserScheduleRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final UserScheduleRepository userScheduleRepository;
    private final UserProfileRepository userProfileRepository;

    // 시간표 저장
    @Transactional
    public ScheduleResponse saveSchedule(Long userId, ScheduleRequest request) {
        UserProfile userProfile = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        List<UserSchedule> schedules = request.getSchedules().stream()
                .map(item -> UserSchedule.builder()
                        .userProfile(userProfile)
                        .dayOfWeek(UserSchedule.DayOfWeek.valueOf(item.getDayOfWeek()))
                        .startTime(LocalTime.parse(item.getStartTime()))
                        .endTime(LocalTime.parse(item.getEndTime()))
                        .build())
                .collect(Collectors.toList());
        userScheduleRepository.saveAll(schedules);

        return toResponse(userProfile.getId(), schedules);
    }

    // 시간표 조회
    @Transactional(readOnly = true)
    public ScheduleResponse getSchedule(Long userId) {
        UserProfile userProfile = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        List<UserSchedule> schedules = userScheduleRepository.findByUserProfile_Id(userProfile.getId());

        return toResponse(userProfile.getId(), schedules);
    }

    // 시간표 수정
    @Transactional
    public ScheduleResponse updateSchedule(Long userId, ScheduleRequest request) {
        UserProfile userProfile = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        userScheduleRepository.deleteByUserProfile_Id(userProfile.getId());
        userScheduleRepository.flush();

        List<UserSchedule> schedules = request.getSchedules().stream()
                .map(item -> UserSchedule.builder()
                        .userProfile(userProfile)
                        .dayOfWeek(UserSchedule.DayOfWeek.valueOf(item.getDayOfWeek()))
                        .startTime(LocalTime.parse(item.getStartTime()))
                        .endTime(LocalTime.parse(item.getEndTime()))
                        .build())
                .collect(Collectors.toList());
        userScheduleRepository.saveAll(schedules);

        return toResponse(userProfile.getId(), schedules);
    }

    private ScheduleResponse toResponse(Long userId, List<UserSchedule> schedules) {
        return ScheduleResponse.builder()
                .userId(userId)
                .schedules(schedules.stream()
                        .map(s -> ScheduleResponse.ScheduleItem.builder()
                                .dayOfWeek(s.getDayOfWeek().name())
                                .startTime(s.getStartTime().toString())
                                .endTime(s.getEndTime().toString())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
