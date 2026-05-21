package com.example.gomplay.domain.survey.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@JsonPropertyOrder({"userId", "personalityType",
        "intensityType", "purposeType", "exerciseTypes",
        "summary", "recommendedExercises", "partnerStyleDescription", "exerciseMoodDescription"})

public class ReportResponse {
    private Long userId;
    private String personalityType;
    private String intensityType;
    private String purposeType;
    private List<String> exerciseTypes;
    private String summary;
    private List<String> recommendedExercises; // 추천 운동 종목
    private String partnerStyleDescription; // 나랑 잘 맞는 파트너 스타일
    private String exerciseMoodDescription; // 나에게 맞는 운동 분위기
}
