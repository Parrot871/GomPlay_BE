package com.example.gomplay.domain.survey.service;

import com.example.gomplay.domain.survey.dto.SurveyRequest;
import com.example.gomplay.domain.survey.dto.SurveyResponse;
import com.example.gomplay.domain.survey.entity.UserSurvey;
import com.example.gomplay.domain.survey.entity.UserSurveyExercise;
import com.example.gomplay.domain.survey.repository.UserSurveyExerciseRepository;
import com.example.gomplay.domain.survey.repository.UserSurveyRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final UserSurveyRepository userSurveyRepository;
    private final UserSurveyExerciseRepository userSurveyExerciseRepository;
    private final UserProfileRepository userProfileRepository;

    // 설문 저장
    @Transactional
    public SurveyResponse saveSurvey(Long userId, SurveyRequest request) {
        UserProfile userProfile = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        UserSurvey survey = UserSurvey.builder()
                .userProfile(userProfile)
                .partnerStyle(UserSurvey.PartnerStyle.valueOf(request.getPartnerStyle()))
                .exerciseIntensity(UserSurvey.ExerciseIntensity.valueOf(request.getExerciseIntensity()))
                .exerciseReason(UserSurvey.ExerciseReason.valueOf(request.getExerciseReason()))
                .build();
        userSurveyRepository.save(survey);

        List<UserSurveyExercise> exercises = request.getExerciseTypes().stream()
                .map(type -> UserSurveyExercise.builder()
                        .userProfile(userProfile)
                        .exerciseType(UserSurveyExercise.ExerciseType.valueOf(type))
                        .build())
                .collect(Collectors.toList());
        userSurveyExerciseRepository.saveAll(exercises);

        return toResponse(userProfile.getId(), survey, exercises);
    }

    // 설문 조회
    @Transactional(readOnly = true)
    public SurveyResponse getSurvey(Long userId) {
        UserProfile userProfile = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        UserSurvey survey = userSurveyRepository.findByUserProfile_Id(userProfile.getId())
                .orElseThrow(() -> new IllegalArgumentException("설문을 찾을 수 없습니다."));

        List<UserSurveyExercise> exercises = userSurveyExerciseRepository.findByUserProfile_Id(userProfile.getId());

        return toResponse(userProfile.getId(), survey, exercises);
    }

    // 설문 수정
    @Transactional
    public SurveyResponse updateSurvey(Long userId, SurveyRequest request) {
        UserProfile userProfile = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        UserSurvey survey = userSurveyRepository.findByUserProfile_Id(userProfile.getId())
                .orElseThrow(() -> new IllegalArgumentException("설문을 찾을 수 없습니다."));

        UserSurvey updatedSurvey = UserSurvey.builder()
                .id(survey.getId())
                .userProfile(userProfile)
                .partnerStyle(UserSurvey.PartnerStyle.valueOf(request.getPartnerStyle()))
                .exerciseIntensity(UserSurvey.ExerciseIntensity.valueOf(request.getExerciseIntensity()))
                .exerciseReason(UserSurvey.ExerciseReason.valueOf(request.getExerciseReason()))
                .build();
        userSurveyRepository.save(updatedSurvey);

        userSurveyExerciseRepository.deleteByUserProfile_Id(userProfile.getId());
        List<UserSurveyExercise> exercises = request.getExerciseTypes().stream()
                .map(type -> UserSurveyExercise.builder()
                        .userProfile(userProfile)
                        .exerciseType(UserSurveyExercise.ExerciseType.valueOf(type))
                        .build())
                .collect(Collectors.toList());
        userSurveyExerciseRepository.saveAll(exercises);

        return toResponse(userProfile.getId(), updatedSurvey, exercises);
    }

    private SurveyResponse toResponse(Long userId, UserSurvey survey, List<UserSurveyExercise> exercises) {
        return SurveyResponse.builder()
                .userId(userId)
                .partnerStyle(survey.getPartnerStyle().name())
                .exerciseIntensity(survey.getExerciseIntensity().name())
                .exerciseReason(survey.getExerciseReason().name())
                .exerciseTypes(exercises.stream()
                        .map(e -> e.getExerciseType().name())
                        .collect(Collectors.toList()))
                .build();
    }
}
