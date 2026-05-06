package com.example.gomplay.domain.matching.service;

import com.example.gomplay.domain.matching.dto.CandidateResponse;
import com.example.gomplay.domain.matching.dto.MatchingToggleResponse;
import com.example.gomplay.domain.matching.entity.QuickMatchLog;
import com.example.gomplay.domain.matching.repository.QuickMatchLogRepository;
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
public class QuickMatchService {

    private final UserProfileRepository userProfileRepository;
    private final QuickMatchLogRepository quickMatchLogRepository;
    private final UserSurveyRepository userSurveyRepository;
    private final UserSurveyExerciseRepository userSurveyExerciseRepository;

    @Transactional
    public MatchingToggleResponse updateMatchingStatus(Long userId, Boolean isMatching) {
        UserProfile userProfile = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        userProfile.updateMatchingStatus(isMatching);

        if (isMatching) {
            quickMatchLogRepository.save(QuickMatchLog.createWaiting(userProfile));
        } else {
            quickMatchLogRepository.findTopByUserProfileIdAndStatus(
                            userProfile.getId(), QuickMatchLog.MatchStatus.WAITING)
                    .ifPresent(QuickMatchLog::cancel);
        }

        String message = isMatching ? "매칭 대기 중입니다." : "매칭이 종료되었습니다.";
        return MatchingToggleResponse.builder()
                .isMatching(isMatching)
                .build();
    }
    @Transactional(readOnly = true)
    public List<CandidateResponse> getCandidates(Long userId) {
        UserProfile me = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        // WAITING 상태인 유저들 조회 (본인 제외)
        List<QuickMatchLog> waitingLogs = quickMatchLogRepository.findByStatus(QuickMatchLog.MatchStatus.WAITING);

        return waitingLogs.stream()
                .map(log -> log.getUserProfile())
                .filter(profile -> !profile.getId().equals(me.getId()))
                .map(profile -> {
                    UserSurvey survey = userSurveyRepository
                            .findByUserProfile_Id(profile.getId())
                            .orElse(null);
                    List<UserSurveyExercise> exercises = userSurveyExerciseRepository
                            .findByUserProfile_Id(profile.getId());
                    return CandidateResponse.of(profile, survey, exercises);
                })
                .collect(Collectors.toList());
    }
}
