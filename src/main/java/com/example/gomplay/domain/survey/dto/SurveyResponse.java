package com.example.gomplay.domain.survey.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@JsonPropertyOrder({"userId", "partnerStyle", "exerciseIntensity", "exerciseReason", "exerciseTypes"})
public class SurveyResponse {
    private Long userId;
    private String partnerStyle;
    private String exerciseIntensity;
    private String exerciseReason;
    private List<String> exerciseTypes;
}