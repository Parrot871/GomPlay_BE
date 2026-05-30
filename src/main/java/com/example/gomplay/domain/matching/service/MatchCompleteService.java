package com.example.gomplay.domain.matching.service;

import com.example.gomplay.domain.matching.entity.MatchResult;
import com.example.gomplay.domain.matching.repository.MatchResultRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchCompleteService {

    private final UserProfileRepository userProfileRepository;
    private final MatchResultRepository matchResultRepository;

    @Transactional
    public void completeMatch(Long userId, Long matchId) {
        UserProfile me = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        completePartner(me, matchId);
    }

    private void completePartner(UserProfile me, Long matchId) {
        MatchResult matchResult = matchResultRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("매칭을 찾을 수 없습니다."));

        if (matchResult.getStatus() != MatchResult.MatchResultStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("진행 중인 매칭이 아닙니다.");
        }

        boolean isUserA = matchResult.getUserA().getId().equals(me.getId());
        boolean isUserB = matchResult.getUserB().getId().equals(me.getId());

        if (!isUserA && !isUserB) {
            throw new IllegalArgumentException("해당 매칭의 참여자가 아닙니다.");
        }

        boolean alreadyCompleted = isUserA
                ? matchResult.isUserACompleted()
                : matchResult.isUserBCompleted();

        if (alreadyCompleted) {
            throw new IllegalArgumentException("이미 완료 요청했습니다.");
        }

        matchResult.markCompleted(me.getId());
    }
}