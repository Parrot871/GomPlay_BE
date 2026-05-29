package com.example.gomplay.domain.matching.service;

import com.example.gomplay.domain.matching.entity.MatchResult;
import com.example.gomplay.domain.matching.repository.MatchResultRepository;
import com.example.gomplay.domain.team.entity.Gathering;
import com.example.gomplay.domain.team.entity.GatheringParticipant;
import com.example.gomplay.domain.team.repository.GatheringParticipantRepository;
import com.example.gomplay.domain.team.repository.GatheringRepository;
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
    private final GatheringRepository gatheringRepository;
    private final GatheringParticipantRepository gatheringParticipantRepository;

    @Transactional
    public void completeMatch(Long userId, Long matchId, String type) {
        UserProfile me = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        if ("PARTNER".equals(type)) {
            completePartner(me, matchId);
        } else if ("GATHERING".equals(type)) {
            completeGathering(me, matchId);
        } else {
            throw new IllegalArgumentException("잘못된 타입입니다.");
        }
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

    private void completeGathering(UserProfile me, Long matchId) {
        Gathering gathering = gatheringRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("모집글을 찾을 수 없습니다."));

        if (gathering.getStatus() != Gathering.Status.OPEN &&
                gathering.getStatus() != Gathering.Status.CLOSED) {
            throw new IllegalArgumentException("진행 중인 모집이 아닙니다.");
        }

        GatheringParticipant participant = gatheringParticipantRepository
                .findByGathering_IdAndUser_Id(gathering.getId(), me.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 모집의 참여자가 아닙니다."));

        if (participant.getStatus() != GatheringParticipant.Status.ACCEPTED) {
            throw new IllegalArgumentException("수락된 참여자가 아닙니다.");
        }
        if (participant.isCompleted()) {
            throw new IllegalArgumentException("이미 완료 요청했습니다.");
        }

        participant.complete();

        boolean allDone = gatheringParticipantRepository
                .findByGathering_Id(gathering.getId())
                .stream()
                .filter(p -> p.getStatus() == GatheringParticipant.Status.ACCEPTED)
                .allMatch(GatheringParticipant::isCompleted);

        if (allDone) {
            gathering.updateStatus(Gathering.Status.COMPLETED);
        }
    }
}
