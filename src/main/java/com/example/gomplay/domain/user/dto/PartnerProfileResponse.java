package com.example.gomplay.domain.user.dto;

import com.example.gomplay.domain.survey.entity.UserSurvey;
import com.example.gomplay.domain.survey.entity.UserSurveyExercise;
import com.example.gomplay.domain.user.entity.UserProfile;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PartnerProfileResponse {

    private Long id;
    private String name;
    private String department;
    private String studentId;
    private BigDecimal mannerTemperature;
    private String profileImageUrl;
    private Integer matchCount;
    private Integer noShowCount;
    private List<String> exerciseTypes;
    private String partnerStyle;
    private String exerciseIntensity;
    private String exerciseReason;

    public PartnerProfileResponse(UserProfile userProfile, UserSurvey survey, List<UserSurveyExercise> exercises) {
        this.id = userProfile.getId();
        this.name = userProfile.getName();
        this.department = userProfile.getDepartment();
        this.studentId = userProfile.getStudentId();
        this.mannerTemperature = userProfile.getMannerTemperature();
        this.profileImageUrl = userProfile.getProfileImageUrl();
        this.matchCount = userProfile.getMatchCount();
        this.noShowCount = userProfile.getNoShowCount();
        this.exerciseTypes = exercises.stream()
                .map(e -> e.getExerciseType().name())
                .collect(Collectors.toList());
        if (survey != null) {
            this.partnerStyle = survey.getPartnerStyle().name();
            this.exerciseIntensity = survey.getExerciseIntensity().name();
            this.exerciseReason = survey.getExerciseReason().name();
        }
    }
}