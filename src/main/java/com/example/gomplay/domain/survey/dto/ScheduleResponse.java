package com.example.gomplay.domain.survey.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ScheduleResponse {
    private Long userId;
    private List<ScheduleItem> schedules;

    @Getter
    @Builder
    public static class ScheduleItem {
        private String dayOfWeek;
        private String startTime;
        private String endTime;
    }
}