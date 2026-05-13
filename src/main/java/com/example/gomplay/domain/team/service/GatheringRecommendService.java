package com.example.gomplay.domain.team.service;

import com.example.gomplay.domain.survey.entity.UserSurveyExercise;
import com.example.gomplay.domain.survey.repository.UserSurveyExerciseRepository;
import com.example.gomplay.domain.team.dto.GatheringRecommendResponse;
import com.example.gomplay.domain.team.entity.Gathering;
import com.example.gomplay.domain.team.repository.GatheringRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GatheringRecommendService {

    private final GatheringRepository gatheringRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSurveyExerciseRepository userSurveyExerciseRepository;

    @Transactional(readOnly = true)
    public List<GatheringRecommendResponse> getRecommendedGatherings(Long userId) {
        UserProfile user = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 유저 선호 운동 종목 가져오기
        List<UserSurveyExercise> userExercises = userSurveyExerciseRepository
                .findByUserProfile_Id(user.getId());

        Set<String> preferredSports = userExercises.stream()
                .map(e -> e.getExerciseType().name())
                .collect(Collectors.toSet());

        // OPEN 상태이고 본인이 host가 아닌 모집글만 조회
        List<Gathering> gatherings = gatheringRepository
                .findByStatusAndHostIdNot(Gathering.Status.OPEN, user.getId());

        return gatherings.stream()
                .map(gathering -> {
                    double score = calculateScore(gathering, user, preferredSports);
                    return new GatheringRecommendResponse(gathering, score);
                })
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());
    }

    private double calculateScore(Gathering gathering, UserProfile user, Set<String> preferredSports) {
        double score = 0;

        // 1. 선호 운동 종목 일치 (35점)
        if (preferredSports.contains(gathering.getSportType())) {
            score += 35;
        }

        // 2. 난이도 일치 (15점)
        String userLevel = getUserLevel(user.getMatchCount());
        if (userLevel.equals(gathering.getDifficulty())) {
            score += 15;
        }

        // 3. 매너온도 (10점)
        if (gathering.getHost().getMannerTemperature() != null) {
            double mannerScore = gathering.getHost().getMannerTemperature().doubleValue();
            score += (mannerScore / 100.0) * 10;
        }

        // 4. 마감 날짜 (10점) - 7일 이내 임박할수록 높은 점수
        long daysUntil = ChronoUnit.DAYS.between(LocalDateTime.now(), gathering.getScheduledAt());
        if (daysUntil <= 7 && daysUntil >= 0) {
            score += (1 - daysUntil / 7.0) * 10;
        }

        // 5. 남은 인원 (5점) - 여유 있을수록 높은 점수
        int remaining = gathering.getMaxParticipants() - gathering.getCurrentParticipants();
        if (remaining > 0) {
            score += ((double) remaining / gathering.getMaxParticipants()) * 5;
        }

        return score;
    }

    private String getUserLevel(Integer matchCount) {
        if (matchCount == null || matchCount <= 5) return "입문자";
        else if (matchCount <= 15) return "초보자";
        else if (matchCount <= 30) return "중급자";
        else if (matchCount <= 50) return "숙련자";
        else return "전문가";
    }
}