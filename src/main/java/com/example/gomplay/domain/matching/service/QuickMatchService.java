package com.example.gomplay.domain.matching.service;

import com.example.gomplay.domain.matching.dto.CandidateResponse;
import com.example.gomplay.domain.matching.dto.MatchRequestDto;
import com.example.gomplay.domain.matching.dto.MatchRequestResponse;
import com.example.gomplay.domain.matching.dto.MatchingToggleResponse;
import com.example.gomplay.domain.matching.entity.MatchRequest;
import com.example.gomplay.domain.matching.entity.MatchResult;
import com.example.gomplay.domain.matching.entity.QuickMatchLog;
import com.example.gomplay.domain.matching.repository.MatchRequestRepository;
import com.example.gomplay.domain.matching.repository.MatchResultRepository;
import com.example.gomplay.domain.matching.repository.QuickMatchLogRepository;
import com.example.gomplay.domain.point.service.PointService;
import com.example.gomplay.domain.survey.entity.UserSurvey;
import com.example.gomplay.domain.survey.entity.UserSurveyExercise;
import com.example.gomplay.domain.survey.repository.UserSurveyExerciseRepository;
import com.example.gomplay.domain.survey.repository.UserSurveyRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import com.example.gomplay.global.websocket.WebSocketSessionRegistry;
import com.example.gomplay.global.websocket.dto.WsMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
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
    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionRegistry sessionRegistry;
    private final MatchScoreCalculator matchScoreCalculator;
    private final MatchResultRepository matchResultRepository;
    private final PointService pointService;

    @Transactional
    public MatchingToggleResponse updateMatchingStatus(Long userId, Boolean isMatching) {
        UserProfile userProfile = userProfileRepository.findByAuthUserIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        if (userProfile.isMatching() == isMatching) {
            return MatchingToggleResponse.builder().isMatching(isMatching).build();
        }

        userProfile.updateMatchingStatus(isMatching);

        if (isMatching) {
            boolean alreadyWaiting = quickMatchLogRepository
                    .findTopByUserProfileIdAndStatus(
                            userProfile.getId(), QuickMatchLog.MatchStatus.WAITING)
                    .isPresent();

            if (!alreadyWaiting) {
                quickMatchLogRepository.save(QuickMatchLog.createWaiting(userProfile));
            }

            // waiting 풀에 추가
            sessionRegistry.addToWaiting(userProfile.getId());

            // 나한테 현재 후보 목록 푸시
            List<CandidateResponse> candidates = buildCandidates(userProfile);
            messagingTemplate.convertAndSendToUser(
                    userProfile.getId().toString(),
                    "/queue/match",
                    WsMessage.builder().type("CANDIDATES_UPDATE").data(candidates).build()
            );

            // 기존 waiting 유저들한테 내 정보 푸시 (각 유저 기준으로 점수/이유 계산)
            sessionRegistry.getWaitingPool().stream()
                    .filter(id -> !id.equals(userProfile.getId()))
                    .forEach(waitingProfileId ->
                            userProfileRepository.findById(waitingProfileId).ifPresent(waitingProfile -> {
                                int scoreAtoB = matchScoreCalculator.calculate(waitingProfile, userProfile);
                                int scoreBtoA = matchScoreCalculator.calculate(userProfile, waitingProfile);
                                int finalScore = (scoreAtoB + scoreBtoA) / 2;
                                List<String> reasons = matchScoreCalculator.getMatchReasons(waitingProfile, userProfile);
                                CandidateResponse myInfo = buildCandidate(userProfile, finalScore, reasons);
                                messagingTemplate.convertAndSendToUser(
                                        waitingProfileId.toString(),
                                        "/queue/match",
                                        WsMessage.builder().type("NEW_CANDIDATE").data(myInfo).build()
                                );
                            })
                    );
        } else {
            quickMatchLogRepository.findAllByUserProfileIdAndStatus(
                            userProfile.getId(), QuickMatchLog.MatchStatus.WAITING)
                    .forEach(QuickMatchLog::cancel);

            // waiting 풀에서 제거
            sessionRegistry.removeFromWaiting(userProfile.getId());

            // 기존 waiting 유저들한테 내가 빠졌다고 푸시
            sessionRegistry.getWaitingPool().forEach(waitingProfileId ->
                    messagingTemplate.convertAndSendToUser(
                            waitingProfileId.toString(),
                            "/queue/match",
                            WsMessage.builder().type("CANDIDATE_LEFT").data(userProfile.getId()).build()
                    )
            );
        }

        return MatchingToggleResponse.builder().isMatching(isMatching).build();
    }

    @Transactional
    public MatchRequestResponse requestMatch(Long userId, MatchRequestDto request) {
        UserProfile requester = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        UserProfile opponent = userProfileRepository.findById(request.getOpponentId())
                .orElseThrow(() -> new IllegalArgumentException("상대방을 찾을 수 없습니다."));

        matchRequestRepository.findByRequester_IdAndOpponent_IdAndStatus(
                        requester.getId(), opponent.getId(), MatchRequest.MatchRequestStatus.PENDING)
                .ifPresent(m -> { throw new IllegalArgumentException("이미 요청 중입니다."); });

        MatchRequest matchRequest = MatchRequest.create(requester, opponent);
        matchRequestRepository.save(matchRequest);

        // 퀵매칭 포인트 차감 -10P
        pointService.addPoint(requester, -10, "quick_match", null);

        MatchRequestResponse response = MatchRequestResponse.builder()
                .matchRequestId(matchRequest.getId())
                .opponentId(opponent.getId())
                .status(matchRequest.getStatus().name())
                .expiresAt(matchRequest.getExpiresAt())
                .build();

        // 상대방한테 매칭 요청 푸시
        messagingTemplate.convertAndSendToUser(
                opponent.getId().toString(),
                "/queue/match",
                WsMessage.builder().type("MATCH_REQUEST").data(response).build()
        );

        return response;
    }

    @Transactional
    public void acceptMatch(Long userId, Long matchRequestId) {
        UserProfile opponent = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        MatchRequest matchRequest = matchRequestRepository.findByIdWithLock(matchRequestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));

        if (!matchRequest.getOpponent().getId().equals(opponent.getId())) {
            throw new IllegalArgumentException("수락 권한이 없습니다.");
        }
        if (matchRequest.isExpired()) {
            matchRequest.timeout();
            throw new IllegalArgumentException("만료된 요청입니다.");
        }
        if (matchRequest.getStatus() != MatchRequest.MatchRequestStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 요청입니다.");
        }
        if (matchRequestRepository.existsAcceptedMatchByUserId(opponent.getId()) ||
                matchRequestRepository.existsAcceptedMatchByUserId(matchRequest.getRequester().getId())) {
            throw new IllegalArgumentException("이미 매칭된 유저입니다.");
        }

        matchRequest.accept();
        matchResultRepository.save(MatchResult.create(
                matchRequest,
                matchRequest.getRequester(),
                opponent
        ));
        UserProfile requester = matchRequest.getRequester();

        // 첫 매칭 여부 확인
        if (requester.getMatchCount() == 0) {
            pointService.addPoint(requester, 30, "first_match", null);
        } else {
            pointService.addPoint(requester, 5, "match_complete", null);
        }

        if (opponent.getMatchCount() == 0) {
            pointService.addPoint(opponent, 30, "first_match", null);
        } else {
            pointService.addPoint(opponent, 5, "match_complete", null);
        }

// matchCount 증가
        requester.incrementMatchCount();
        opponent.incrementMatchCount();

        quickMatchLogRepository.findTopByUserProfileIdAndStatus(
                        opponent.getId(), QuickMatchLog.MatchStatus.WAITING)
                .ifPresent(log -> log.updateStatus(QuickMatchLog.MatchStatus.MATCHED));
        quickMatchLogRepository.findTopByUserProfileIdAndStatus(
                        matchRequest.getRequester().getId(), QuickMatchLog.MatchStatus.WAITING)
                .ifPresent(log -> log.updateStatus(QuickMatchLog.MatchStatus.MATCHED));

        opponent.updateMatchingStatus(false);
        matchRequest.getRequester().updateMatchingStatus(false);

        // waiting 풀에서 양쪽 제거
        sessionRegistry.removeFromWaiting(opponent.getId());
        sessionRegistry.removeFromWaiting(matchRequest.getRequester().getId());

        // 양쪽 waiting 유저들한테 두 명이 빠졌다고 푸시
        sessionRegistry.getWaitingPool().forEach(waitingProfileId -> {
            messagingTemplate.convertAndSendToUser(
                    waitingProfileId.toString(),
                    "/queue/match",
                    WsMessage.builder().type("CANDIDATE_LEFT").data(opponent.getId()).build()
            );
            messagingTemplate.convertAndSendToUser(
                    waitingProfileId.toString(),
                    "/queue/match",
                    WsMessage.builder().type("CANDIDATE_LEFT").data(matchRequest.getRequester().getId()).build()
            );
        });

        // 요청자한테 수락 푸시
        messagingTemplate.convertAndSendToUser(
                matchRequest.getRequester().getId().toString(),
                "/queue/match",
                WsMessage.builder().type("MATCH_ACCEPTED").data(matchRequestId).build()
        );
    }

    @Transactional
    public void rejectMatch(Long userId, Long matchRequestId) {
        UserProfile opponent = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        MatchRequest matchRequest = matchRequestRepository.findByIdWithLock(matchRequestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));

        if (!matchRequest.getOpponent().getId().equals(opponent.getId())) {
            throw new IllegalArgumentException("거절 권한이 없습니다.");
        }
        if (matchRequest.isExpired()) {
            matchRequest.timeout();
            throw new IllegalArgumentException("만료된 요청입니다.");
        }
        if (matchRequest.getStatus() != MatchRequest.MatchRequestStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 요청입니다.");
        }

        matchRequest.reject();

        // 요청자한테 거절 푸시
        messagingTemplate.convertAndSendToUser(
                matchRequest.getRequester().getId().toString(),
                "/queue/match",
                WsMessage.builder().type("MATCH_REJECTED").data(matchRequestId).build()
        );
    }

    // -- 현재 waiting 풀에서 후보 목록 빌드 (본인 제외, 양방향 점수 계산 후 내림차순 정렬)
    private List<CandidateResponse> buildCandidates(UserProfile me) {
        return sessionRegistry.getWaitingPool().stream()
                .filter(id -> !id.equals(me.getId()))
                .map(id -> userProfileRepository.findById(id).orElse(null))
                .filter(profile -> profile != null && profile.isMatching())
                .map(profile -> {
                    int scoreAtoB = matchScoreCalculator.calculate(me, profile);
                    int scoreBtoA = matchScoreCalculator.calculate(profile, me);
                    int finalScore = (scoreAtoB + scoreBtoA) / 2;
                    List<String> reasons = matchScoreCalculator.getMatchReasons(me, profile);
                    return buildCandidate(profile, finalScore, reasons);
                })
                .sorted(Comparator.comparingInt(CandidateResponse::getMatchScore).reversed())
                .collect(Collectors.toList());
    }

    // -- 단일 유저 CandidateResponse 빌드
    private CandidateResponse buildCandidate(UserProfile profile, int matchScore, List<String> reasons) {
        UserSurvey survey = userSurveyRepository
                .findByUserProfile_Id(profile.getId())
                .orElse(null);
        List<UserSurveyExercise> exercises = userSurveyExerciseRepository
                .findByUserProfile_Id(profile.getId());
        return CandidateResponse.of(profile, survey, exercises, matchScore, reasons);
    }

    public CandidateResponse getNextCandidate(Long userId, List<Long> excludeIds) {
        UserProfile me = userProfileRepository.findByAuthUser_Id(userId).orElseThrow();
        return sessionRegistry.getWaitingPool().stream()
                .filter(id -> !id.equals(me.getId()))
                .filter(id -> !excludeIds.contains(id))
                .map(id -> userProfileRepository.findById(id).orElse(null))
                .filter(profile -> profile != null && profile.isMatching())
                .map(profile -> {
                    int scoreAtoB = matchScoreCalculator.calculate(me, profile);
                    int scoreBtoA = matchScoreCalculator.calculate(profile, me);
                    int finalScore = (scoreAtoB + scoreBtoA) / 2;
                    List<String> reasons = matchScoreCalculator.getMatchReasons(me, profile);
                    return buildCandidate(profile, finalScore, reasons);
                })
                .max(Comparator.comparingInt(CandidateResponse::getMatchScore))
                .orElse(null);
    }
}