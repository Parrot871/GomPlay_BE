package com.example.gomplay.domain.survey.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@JsonPropertyOrder({"userId", "schedules"})
public class ScheduleResponse {
    private Long userId;
    private List<ScheduleItem> schedules;

    @Getter
    @Builder
    @JsonPropertyOrder({"dayOfWeek", "startTime", "endTime"})
    public static class ScheduleItem {
        private String dayOfWeek;
        private String startTime;
        private String endTime;
    }
}