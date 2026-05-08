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

    // QuickMatchService.java
    @Transactional
    public MatchingToggleResponse updateMatchingStatus(Long userId, Boolean isMatching) {
        // Lock이 걸린 조회 사용
        UserProfile userProfile = userProfileRepository.findByAuthUserIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        // 현재 상태와 요청 상태가 같으면 굳이 DB를 또 찌를 필요 없이 바로 리턴 (최적화 방어)
        if (userProfile.isMatching() == isMatching) {
            return MatchingToggleResponse.builder().isMatching(isMatching).build();
        }

        userProfile.updateMatchingStatus(isMatching);

        if (isMatching) {
            // 이미 WAITING이 있으면 INSERT 안 함
            boolean alreadyWaiting = quickMatchLogRepository
                    .findTopByUserProfileIdAndStatus(
                            userProfile.getId(), QuickMatchLog.MatchStatus.WAITING)
                    .isPresent();

            if (!alreadyWaiting) {
                quickMatchLogRepository.save(QuickMatchLog.createWaiting(userProfile));
            }
        } else {
            // WAITING 전부 CANCELLED로 변경
            quickMatchLogRepository.findAllByUserProfileIdAndStatus(
                            userProfile.getId(), QuickMatchLog.MatchStatus.WAITING)
                    .forEach(QuickMatchLog::cancel);
        }

        return MatchingToggleResponse.builder()
                .isMatching(isMatching)
                .build();
    }
    @Transactional(readOnly = true)
    public List<CandidateResponse> getCandidates(Long userId) {
        UserProfile me = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        List<QuickMatchLog> waitingLogs = quickMatchLogRepository.findByStatus(QuickMatchLog.MatchStatus.WAITING);

        return waitingLogs.stream()
                .map(QuickMatchLog::getUserProfile)
                .filter(profile -> !profile.getId().equals(me.getId())) // 본인 제외
                .filter(UserProfile::isMatching) // is_matching = 1인 것만
                .distinct() // 중복 제거
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

    // 수락
    @Transactional
    public void acceptMatch(Long userId, Long matchRequestId) {
        UserProfile opponent = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        // 1. 락 걸고 요청 조회
        MatchRequest matchRequest = matchRequestRepository.findByIdWithLock(matchRequestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));

        // 2. 내가 opponent인지 확인
        if (!matchRequest.getOpponent().getId().equals(opponent.getId())) {
            throw new IllegalArgumentException("수락 권한이 없습니다.");
        }

        // 3. 만료 여부 확인
        if (matchRequest.isExpired()) {
            matchRequest.timeout();
            throw new IllegalArgumentException("만료된 요청입니다.");
        }

        // 4. 이미 처리된 요청인지 확인
        if (matchRequest.getStatus() != MatchRequest.MatchRequestStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 요청입니다.");
        }

        // 5. 나 또는 상대방이 이미 다른 매칭에서 ACCEPTED 됐는지 확인
        if (matchRequestRepository.existsAcceptedMatchByUserId(opponent.getId()) ||
                matchRequestRepository.existsAcceptedMatchByUserId(matchRequest.getRequester().getId())) {
            throw new IllegalArgumentException("이미 매칭된 유저입니다.");
        }

        // 6. 수락 처리
        matchRequest.accept();

        // 7. quick_match_log 양쪽 MATCHED
        quickMatchLogRepository.findTopByUserProfileIdAndStatus(
                        opponent.getId(), QuickMatchLog.MatchStatus.WAITING)
                .ifPresent(log -> log.updateStatus(QuickMatchLog.MatchStatus.MATCHED));
        quickMatchLogRepository.findTopByUserProfileIdAndStatus(
                        matchRequest.getRequester().getId(), QuickMatchLog.MatchStatus.WAITING)
                .ifPresent(log -> log.updateStatus(QuickMatchLog.MatchStatus.MATCHED));

        // 8. user_profile 양쪽 is_matching = 0
        opponent.updateMatchingStatus(false);
        matchRequest.getRequester().updateMatchingStatus(false);
    }

    // 거절
    @Transactional
    public void rejectMatch(Long userId, Long matchRequestId) {
        UserProfile opponent = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        MatchRequest matchRequest = matchRequestRepository.findByIdWithLock(matchRequestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));

        // 1. 내가 opponent인지 확인
        if (!matchRequest.getOpponent().getId().equals(opponent.getId())) {
            throw new IllegalArgumentException("거절 권한이 없습니다.");
        }

        // 2. 만료 여부 확인
        if (matchRequest.isExpired()) {
            matchRequest.timeout();
            throw new IllegalArgumentException("만료된 요청입니다.");
        }

        // 3. 이미 처리된 요청인지 확인
        if (matchRequest.getStatus() != MatchRequest.MatchRequestStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 요청입니다.");
        }

        matchRequest.reject();
    }
}
