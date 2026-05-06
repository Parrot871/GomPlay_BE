package com.example.gomplay.domain.matching.dto;

import com.example.gomplay.domain.survey.entity.UserSurvey;
import com.example.gomplay.domain.survey.entity.UserSurveyExercise;
import com.example.gomplay.domain.user.entity.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public static CandidateResponse of(UserProfile profile, UserSurvey survey, List<UserSurveyExercise> exercises) {
        return CandidateResponse.builder()
                .userProfileId(profile.getId())
                .name(profile.getName())
                .profileImageUrl(profile.getProfileImageUrl())
                .department(profile.getDepartment())
                .studentId(profile.getStudentId())
                .partnerStyle(survey != null ? survey.getPartnerStyle().name() : null)
                .exerciseIntensity(survey != null ? survey.getExerciseIntensity().name() : null)
                .exerciseTypes(exercises.stream()
                        .map(e -> e.getExerciseType().name())
                        .collect(Collectors.toList()))
                .build();
    }
}