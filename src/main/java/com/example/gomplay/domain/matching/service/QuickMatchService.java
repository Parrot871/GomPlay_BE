package com.example.gomplay.domain.matching.service;

import com.example.gomplay.domain.matching.dto.CandidateResponse;
import com.example.gomplay.domain.matching.dto.MatchRequestDto;
import com.example.gomplay.domain.matching.dto.MatchRequestResponse;
import com.example.gomplay.domain.matching.dto.MatchingToggleResponse;
import com.example.gomplay.domain.matching.entity.MatchRequest;
import com.example.gomplay.domain.matching.entity.QuickMatchLog;
import com.example.gomplay.domain.matching.repository.MatchRequestRepository;
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
    private final MatchRequestRepository matchRequestRepository;

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

    @Transactional
    public MatchRequestResponse requestMatch(Long userId, MatchRequestDto request) {
        UserProfile requester = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        UserProfile opponent = userProfileRepository.findById(request.getOpponentId())
                .orElseThrow(() -> new IllegalArgumentException("상대방을 찾을 수 없습니다."));

        // 이미 PENDING 요청이 있는지 확인
        matchRequestRepository.findByRequester_IdAndOpponent_IdAndStatus(
                        requester.getId(), opponent.getId(), MatchRequest.MatchRequestStatus.PENDING)
                .ifPresent(m -> { throw new IllegalArgumentException("이미 요청 중입니다."); });

        MatchRequest matchRequest = MatchRequest.create(requester, opponent);
        matchRequestRepository.save(matchRequest);

        return MatchRequestResponse.builder()
                .matchRequestId(matchRequest.getId())
                .opponentId(opponent.getId())
                .status(matchRequest.getStatus().name())
                .expiresAt(matchRequest.getExpiresAt())
                .build();
    }
}
