package com.example.gomplay.domain.survey.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class SurveyResponse {
    private Long userId;
    private String partnerStyle;
    private String exerciseIntensity;
    private String exerciseReason;
    private List<String> exerciseTypes;
}