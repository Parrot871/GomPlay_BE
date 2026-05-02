package com.example.gomplay.domain.survey.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class SurveyRequest {
    private String partnerStyle;
    private String exerciseIntensity;
    private String exerciseReason;
    private List<String> exerciseTypes;
}
