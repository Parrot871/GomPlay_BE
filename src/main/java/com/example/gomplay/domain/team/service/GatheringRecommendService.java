package com.example.gomplay.domain.team.service;

import com.example.gomplay.domain.survey.entity.UserSchedule;
import com.example.gomplay.domain.survey.entity.UserSurveyExercise;
import com.example.gomplay.domain.survey.repository.UserScheduleRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GatheringRecommendService {

    private final GatheringRepository gatheringRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSurveyExerciseRepository userSurveyExerciseRepository;
    private final UserScheduleRepository userScheduleRepository;

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

        // 유저 시간표 가져오기
        List<UserSchedule> userSchedules = userScheduleRepository
                .findByUserProfile_Id(user.getId());

        // OPEN 상태이고 본인이 host가 아닌 모집글만 조회
        List<Gathering> gatherings = gatheringRepository
                .findByStatusAndHostIdNot(Gathering.Status.OPEN, user.getId());

        return gatherings.stream()
                .map(gathering -> {
                    double score = calculateScore(gathering, user, preferredSports, userSchedules);
                    return new GatheringRecommendResponse(gathering, score);
                })
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());
    }

    private double calculateScore(Gathering gathering, UserProfile user,
                                   Set<String> preferredSports, List<UserSchedule> userSchedules) {
        double score = 0;

        // 1. 선호 운동 종목 - 자카드 유사도 (35점)
        Set<String> gatheringSportSet = new HashSet<>();
        gatheringSportSet.add(gathering.getSportType());

        Set<String> union = new HashSet<>(preferredSports);
        union.addAll(gatheringSportSet);

        Set<String> intersection = new HashSet<>(preferredSports);
        intersection.retainAll(gatheringSportSet);

        double jaccardScore = union.isEmpty() ? 0 :
                (double) intersection.size() / union.size();
        score += jaccardScore * 35;

        // 2. 시간표 일치 (25점) - 수업 시간과 겹치지 않으면 점수
        if (isAvailable(gathering.getScheduledAt(), gathering.getScheduledEndAt(), userSchedules)) {
            score += 25;
        }

        // 3. 난이도 일치 (15점)
        String userLevel = getUserLevel(user.getMatchCount());
        if (userLevel.equals(gathering.getDifficulty())) {
            score += 15;
        }

        // 4. 매너온도 (10점)
        if (gathering.getHost().getMannerTemperature() != null) {
            double mannerScore = gathering.getHost().getMannerTemperature().doubleValue();
            score += (mannerScore / 100.0) * 10;
        }

        // 5. 마감 날짜 (10점) - 7일 이내 임박할수록 높은 점수
        long daysUntil = ChronoUnit.DAYS.between(LocalDateTime.now(), gathering.getScheduledAt());
        if (daysUntil <= 7 && daysUntil >= 0) {
            score += (1 - daysUntil / 7.0) * 10;
        }

        // 6. 남은 인원 (5점) - 여유 있을수록 높은 점수
        int remaining = gathering.getMaxParticipants() - gathering.getCurrentParticipants();
        if (remaining > 0) {
            score += ((double) remaining / gathering.getMaxParticipants()) * 5;
        }

        // 7. 노쇼 패널티 - 방장 기준
        score += getNoShowPenalty(gathering.getHost().getNoShowCount(), true);

        return score;
    }

    private double getNoShowPenalty(Integer noShowCount, boolean isHost) {
        if (noShowCount == null || noShowCount == 0) return 0;

        if (isHost) {
            // 방장 노쇼 패널티 (더 강하게)
            if (noShowCount <= 2) return -10;
            else if (noShowCount <= 5) return -25;
            else return -40;
        } else {
            // 일반 참여자 노쇼 패널티
            if (noShowCount <= 2) return -5;
            else if (noShowCount <= 5) return -15;
            else return -25;
        }
    }

    private boolean isAvailable(LocalDateTime scheduledAt, LocalDateTime scheduledEndAt,
                                  List<UserSchedule> userSchedules) {
        if (userSchedules.isEmpty()) return true;

        String gatheringDay = scheduledAt.getDayOfWeek().name().substring(0, 3);

        for (UserSchedule schedule : userSchedules) {
            if (!schedule.getDayOfWeek().name().equals(gatheringDay)) continue;

            boolean overlaps = scheduledAt.toLocalTime().isBefore(schedule.getEndTime())
                    && scheduledEndAt.toLocalTime().isAfter(schedule.getStartTime());

            if (overlaps) return false;
        }
        return true;
    }

    private String getUserLevel(Integer matchCount) {
        if (matchCount == null || matchCount <= 5) return "입문자";
        else if (matchCount <= 15) return "초보자";
        else if (matchCount <= 30) return "중급자";
        else if (matchCount <= 50) return "숙련자";
        else return "전문가";
    }
}