package com.example.gomplay.domain.matching.service;

import com.example.gomplay.domain.survey.entity.UserSchedule;
import com.example.gomplay.domain.survey.entity.UserSurvey;
import com.example.gomplay.domain.survey.repository.UserSurveyRepository;
import com.example.gomplay.domain.survey.repository.UserSurveyExerciseRepository;
import com.example.gomplay.domain.survey.repository.UserScheduleRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 추천 알고리즘 점수 계산기 (0 ~ 100점)
 *
 * 가중치:
 *    시간표 공강 일치 30%
 *    선호 운동 종목 25%
 *    성향 일치 20%
 *    매너온도 15%
 *    학과 일치 5%
 *    학번 일치 5%
 */
@Component
@RequiredArgsConstructor
public class Matchscorecalculator {
    private static final double WEIGHT_SCHEDULE = 0.30;
    private static final double WEIGHT_EXERCISE = 0.25;
    private static final double WEIGHT_PERSONALITY = 0.20;
    private static final double WEIGHT_MANNER = 0.15;
    private static final double WEIGHT_DEPARTMENT = 0.05;
    private static final double WEIGHT_STUDENT_ID = 0.05;

    private static final LocalTime DAY_START = LocalTime.of(9,0);
    private static final LocalTime DAY_END = LocalTime.of(18, 0);
    private static final long TOTAL_DAY_MINUTES = ChronoUnit.MINUTES.between(DAY_START, DAY_END);
    private static final int DAY_COUNT = 5;
    private static final long TOTAL_MINUTES = TOTAL_DAY_MINUTES * DAY_COUNT;

    private final UserScheduleRepository scheduleRepository;
    private final UserSurveyRepository surveyRepository;
    private final UserSurveyExerciseRepository surveyExerciseRepository;

    public int calculate(UserProfile me, UserProfile other) {
        UserSurvey mySurvey = surveyRepository.findByUserProfile_Id(me.getId()).orElse(null);
        UserSurvey otherSurvey = surveyRepository.findByUserProfile_Id(other.getId()).orElse(null);

        double score = 0;
        score += scheduleScore(me, other)    * WEIGHT_SCHEDULE;
        score += exerciseScore(me, other)   * WEIGHT_EXERCISE;
        score += personalityScore(mySurvey, otherSurvey) * WEIGHT_PERSONALITY;
        score += mannerScore(other)      * WEIGHT_MANNER;
        score += departmentScore(me, other)  * WEIGHT_DEPARTMENT;
        score += studentIdScore(me, other)   * WEIGHT_STUDENT_ID;

        return (int)Math.round(score);
    }

    // -- 1. 시간표 공강 일치 (30%) -------------------------------------------
    // 수업이 없는 구간(공강)을 요일별로 구하고 두 사람의 공강이 겹치는 총
    private static final List<UserSchedule.DayOfWeek> WEEKDAYS = List.of(
            UserSchedule.DayOfWeek.MON,
            UserSchedule.DayOfWeek.TUE,
            UserSchedule.DayOfWeek.WED,
            UserSchedule.DayOfWeek.THU,
            UserSchedule.DayOfWeek.FRI
    );
    private double scheduleScore(UserProfile me, UserProfile other) {
        Map<UserSchedule.DayOfWeek, List<UserSchedule>> myMap =
                groupByDay(scheduleRepository.findByUserProfile_Id(me.getId()));
        Map<UserSchedule.DayOfWeek, List<UserSchedule>> otherMap =
                groupByDay(scheduleRepository.findByUserProfile_Id(other.getId()));

        long overlapMinutes = 0;
        long totalPossibleMinutes = 0;

        for (UserSchedule.DayOfWeek day : WEEKDAYS) {
            List<UserSchedule> myClasses = myMap.getOrDefault(day, Collections.emptyList());
            List<UserSchedule> otherClasses = otherMap.getOrDefault(day, Collections.emptyList());

            // 둘 중 하나라도 수업 없는 날은 스킵 → 0점
            if (myClasses.isEmpty() || otherClasses.isEmpty()) continue;

            List<long[]> myFree = getReliableFreeSlots(myClasses);
            List<long[]> otherFree = getReliableFreeSlots(otherClasses);

            if (myFree.isEmpty() || otherFree.isEmpty()) continue;

            overlapMinutes += intersectMinutes(myFree, otherFree);
            totalPossibleMinutes += unionMinutes(myFree, otherFree);
        }

        if (totalPossibleMinutes == 0) return 0;
        return Math.min((double) overlapMinutes / totalPossibleMinutes, 1.0) * 100;
    }

    private Map<UserSchedule.DayOfWeek, List<UserSchedule>> groupByDay(List<UserSchedule> schedules){
        return schedules.stream().collect(Collectors.groupingBy(UserSchedule::getDayOfWeek));
    }

    // 수업 있는 날만: 첫수업~마지막수업 사이 공강만 반환
    private List<long[]> getReliableFreeSlots(List<UserSchedule> schedules) {
        // 수업 없는 날은 제외
        if (schedules.isEmpty()) return Collections.emptyList();

        List<UserSchedule> sorted = schedules.stream()
                .sorted(Comparator.comparing(UserSchedule::getStartTime))
                .collect(Collectors.toList());

        List<long[]> freeSlots = new ArrayList<>();
        LocalTime cursor = DAY_START; // 윈도우를 하루 전체로 변경

        for (UserSchedule s : sorted) {
            LocalTime classStart = s.getStartTime().isBefore(DAY_START) ? DAY_START : s.getStartTime();
            LocalTime classEnd = s.getEndTime().isAfter(DAY_END) ? DAY_END : s.getEndTime();

            if (cursor.isBefore(classStart)) {
                freeSlots.add(new long[]{
                        ChronoUnit.MINUTES.between(DAY_START, cursor),
                        ChronoUnit.MINUTES.between(DAY_START, classStart)
                });
            }
            if (classEnd.isAfter(cursor)) cursor = classEnd;
        }
        // 마지막 수업 이후 ~ DAY_END 도 공강으로 포함
        if (cursor.isBefore(DAY_END)) {
            freeSlots.add(new long[]{
                    ChronoUnit.MINUTES.between(DAY_START, cursor),
                    TOTAL_DAY_MINUTES
            });
        }
        return freeSlots;
    }

    private long unionMinutes(List<long[]> a, List<long[]> b) {
        List<long[]> merged = new ArrayList<>();
        merged.addAll(a);
        merged.addAll(b);
        merged.sort(Comparator.comparingLong(s -> s[0]));

        long total = 0;
        long curStart = -1, curEnd = -1;
        for (long[] slot : merged) {
            if (slot[0] > curEnd) {
                if (curEnd != -1) total += curEnd - curStart;
                curStart = slot[0];
                curEnd = slot[1];
            } else {
                curEnd = Math.max(curEnd, slot[1]);
            }
        }
        if (curEnd != -1) total += curEnd - curStart;
        return total;
    }

    private long intersectMinutes(List<long[]> a, List<long[]> b){
        long total = 0;
        for (long[] sa : a) {
            for (long[] sb : b){
                long start = Math.max(sa[0], sb[0]);
                long end = Math.min(sa[1], sb[1]);
                System.out.printf("    intersect [%d,%d] ∩ [%d,%d] = start:%d end:%d%n",
                        sa[0],sa[1],sb[0],sb[1],start,end);
                if(end > start) total += (end-start);
            }
        }
        return total;
    }

    // -- 2. 선호 운동 종목 일치 (25%) -------------------------------------------
    // Jaccard 유사도(교집합 크기 / 합집합 크기) 이용하여 점수 계산 -> 한계는 양방향 점수 계산을 통해 보완
    private double exerciseScore(UserProfile me, UserProfile other){
        Set<String> myTypes = surveyExerciseRepository.findByUserProfile_Id(me.getId())
                .stream().map(e -> e.getExerciseType().name()).collect(Collectors.toSet());
        Set<String> otherTypes = surveyExerciseRepository.findByUserProfile_Id(other.getId())
                .stream().map(e -> e.getExerciseType().name()).collect(Collectors.toSet());
        if (myTypes.isEmpty() && otherTypes.isEmpty()) return 50;
        Set<String> union = new HashSet<>(myTypes);
        union.addAll(otherTypes);
        Set<String> intersection = new HashSet<>(myTypes);
        intersection.retainAll(otherTypes);
        return ((double) intersection.size() / union.size()) * 100;
    }
    // -- 3. 성향 일치 (20%) -------------------------------------------
    // 운동 스타일(34점) + 운동 강도(33점) + 운동 이유(33점)
    private double personalityScore(UserSurvey me, UserSurvey other){
        if (me == null || other == null) return 0;

        double score = 0;
        // 운동 스타일: 단순 일치/불일치
        if (me.getPartnerStyle() != null && me.getPartnerStyle().equals(other.getPartnerStyle()))
            score += 34;
        // 운동 강도: 단계 차이 기반 부분 점수
        if (me.getExerciseIntensity() != null && other.getExerciseIntensity() != null) {
            int diff = Math.abs(me.getExerciseIntensity().ordinal() - other.getExerciseIntensity().ordinal());
            if (diff == 0) score += 33;
            if (diff == 1) score += 16;
            // diff >= 2 → 0점
        }
        // 운동 스타일: 단순 일치/불일치
        if(me.getExerciseReason() != null && me.getExerciseReason().equals(other.getExerciseReason()))
            score += 33;
        return score;
    }
    // -- 4. 매너온도 (15%) -------------------------------------------
    // 선형 방식의 매너온도 점수
    private double mannerScore(UserProfile other){
        double temp = other.getMannerTemperature() != null
                ? other.getMannerTemperature().doubleValue()
                : 36.5;
        return Math.min(temp, 100);
    }
    // -- 5. 학과/단과대 일치 (5%) ------------------------------------------
    // 동일 학과인 경우 100점, 동일 단과대인 경우 60점, 그 외의 경우 0점
    private double departmentScore(UserProfile me, UserProfile other) {
        if (me.getDepartment() == null || other.getDepartment() == null) return 0;

        // 동일 학과
        if (me.getDepartment().equals(other.getDepartment())) return 100;

        // 동일 단과대
        if (me.getCollege() != null && other.getCollege() != null
                && me.getCollege().equals(other.getCollege())) return 60;

        return 0;
    }
    // -- 6. 학번 일치 (5%) -------------------------------------------
    // 차이가 0일 경우 100점, 차이가 1일 경우 70점, 차이가 2일 경우 50점, 차이가 3 이상일 경우 20점
    private double studentIdScore(UserProfile me, UserProfile other) {
        if (me.getStudentId() == null || other.getStudentId() == null) return 0;

        try {
            int myYear    = Integer.parseInt(me.getStudentId().replaceAll("[^0-9]", ""));
            int otherYear = Integer.parseInt(other.getStudentId().replaceAll("[^0-9]", ""));
            int diff = Math.abs(myYear - otherYear);

            if (diff == 0) return 100;
            if (diff == 1) return 70;
            if (diff == 2) return 50;
            return 20;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // 추천 이유 생성
    public List<String> getMatchReasons(UserProfile me, UserProfile other) {
        UserSurvey mySurvey    = surveyRepository.findByUserProfile_Id(me.getId()).orElse(null);
        UserSurvey otherSurvey = surveyRepository.findByUserProfile_Id(other.getId()).orElse(null);

        List<String> reasons = new ArrayList<>();

        // 시간표
        if (scheduleScore(me, other) >= 50)
            reasons.add("공강 시간이 잘 맞아요");

        // 운동 종목
        double exScore = exerciseScore(me, other);
        if (exScore >= 80)
            reasons.add("선호 운동 종목이 잘 맞아요");
        else if (exScore >= 50)
            reasons.add("선호 운동 종목이 일부 겹쳐요");

        // 성향
        if (mySurvey != null && otherSurvey != null) {
            if (mySurvey.getPartnerStyle() != null
                    && mySurvey.getPartnerStyle().equals(otherSurvey.getPartnerStyle()))
                reasons.add("운동 스타일이 같아요");
            if (mySurvey.getExerciseIntensity() != null
                    && mySurvey.getExerciseIntensity().equals(otherSurvey.getExerciseIntensity()))
                reasons.add("운동 강도가 같아요");
            if (mySurvey.getExerciseReason() != null
                    && mySurvey.getExerciseReason().equals(otherSurvey.getExerciseReason()))
                reasons.add("운동 목적이 같아요");
        }

        // 학과/단과대
        if (me.getDepartment() != null && me.getDepartment().equals(other.getDepartment()))
            reasons.add("같은 학과예요");
        else if (me.getCollege() != null && me.getCollege().equals(other.getCollege()))
            reasons.add("같은 단과대 소속이에요");

        // 학번
        try {
            int diff = Math.abs(Integer.parseInt(me.getStudentId())
                    - Integer.parseInt(other.getStudentId()));
            if (diff == 0) reasons.add("같은 학번이에요");
            else if (diff <= 2) reasons.add("비슷한 학번이에요");
        } catch (Exception ignored) {}

        return reasons;
    }

}
