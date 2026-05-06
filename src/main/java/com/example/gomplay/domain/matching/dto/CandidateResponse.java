package com.example.gomplay.domain.matching.dto;

import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.survey.entity.UserSurvey;
import com.example.gomplay.domain.survey.entity.UserSurveyExercise;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {
    private Long userProfileId;
    private String name;
    private String profileImageUrl;
    private String department;
    private String studentId;
    private String partnerStyle;
    private String exerciseIntensity;
    private List<String> exerciseTypes;
}