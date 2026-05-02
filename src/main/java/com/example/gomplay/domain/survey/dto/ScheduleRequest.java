package com.example.gomplay.domain.survey.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class ScheduleRequest {
    private List<ScheduleItem> schedules;

    @Getter
    public static class ScheduleItem {
        private String dayOfWeek;
        private String startTime;
        private String endTime;
    }
}
