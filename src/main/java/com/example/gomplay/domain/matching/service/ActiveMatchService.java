package com.example.gomplay.domain.matching.service;

import com.example.gomplay.domain.chat.entity.ChatRoom;
import com.example.gomplay.domain.chat.repository.ChatRoomRepository;
import com.example.gomplay.domain.matching.dto.ActiveMatchResponse;
import com.example.gomplay.domain.matching.dto.MatchHistoryResponse;
import com.example.gomplay.domain.matching.entity.MatchResult;
import com.example.gomplay.domain.matching.repository.MatchResultRepository;
import com.example.gomplay.domain.review.repository.ReviewRepository;
import com.example.gomplay.domain.team.entity.Gathering;
import com.example.gomplay.domain.team.entity.GatheringParticipant;
import com.example.gomplay.domain.team.repository.GatheringParticipantRepository;
import com.example.gomplay.domain.team.repository.GatheringRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActiveMatchService {

    private final UserProfileRepository userProfileRepository;
    private final GatheringRepository gatheringRepository;
    private final GatheringParticipantRepository gatheringParticipantRepository;
    private final MatchResultRepository matchResultRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ReviewRepository reviewRepository;

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
            .withZone(ZoneId.of("Asia/Seoul"));

    @Transactional(readOnly = true)
    public List<ActiveMatchResponse> getActiveMatches(Long userId) {
        UserProfile me = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        List<ActiveMatchResponse> result = new ArrayList<>();

        // GATHERING - 내가 HOST인 OPEN 모집글
        gatheringRepository.findByHost_Id(me.getId())
                .stream()
                .filter(g -> g.getStatus() == Gathering.Status.OPEN)
                .forEach(gathering -> {
                    GatheringParticipant acceptedParticipant = gatheringParticipantRepository
                            .findByGathering_Id(gathering.getId())
                            .stream()
                            .filter(p -> p.getStatus() == GatheringParticipant.Status.ACCEPTED)
                            .findFirst()
                            .orElse(null);

                    UserProfile partner = acceptedParticipant != null ? acceptedParticipant.getUser() : null;

                    long pendingCount = gatheringParticipantRepository
                            .countByGathering_IdAndStatus(gathering.getId(), GatheringParticipant.Status.PENDING);
                    boolean reviewed = reviewRepository
                            .existsByReviewer_IdAndGatheringId(me.getId(), gathering.getId());

                    result.add(ActiveMatchResponse.ofGathering(gathering, me, partner, pendingCount, reviewed));
                });

        // GATHERING - 내가 ACCEPTED 참여자인 OPEN 모집글 (본인이 HOST인 경우 제외)
        gatheringParticipantRepository.findByUser_IdAndStatus(me.getId(), GatheringParticipant.Status.ACCEPTED)
                .stream()
                .filter(p -> p.getGathering().getStatus() == Gathering.Status.OPEN)
                .filter(p -> !p.getGathering().getHost().getId().equals(me.getId()))
                .forEach(participant -> {
                    Gathering gathering = participant.getGathering();
                    long pendingCount = gatheringParticipantRepository
                            .countByGathering_IdAndStatus(gathering.getId(), GatheringParticipant.Status.PENDING);
                    boolean reviewed = reviewRepository
                            .existsByReviewer_IdAndGatheringId(me.getId(), gathering.getId());

                    result.add(ActiveMatchResponse.ofGathering(gathering, me, gathering.getHost(), pendingCount, reviewed));
                });

        // GATHERING - 내가 PENDING 신청자인 OPEN 모집글
        gatheringParticipantRepository.findByUser_IdAndStatus(me.getId(), GatheringParticipant.Status.PENDING)
                .stream()
                .filter(p -> p.getGathering().getStatus() == Gathering.Status.OPEN)
                .forEach(participant -> {
                    Gathering gathering = participant.getGathering();
                    boolean reviewed = reviewRepository
                            .existsByReviewer_IdAndGatheringId(me.getId(), gathering.getId());

                    result.add(ActiveMatchResponse.ofGatheringPending(
                            gathering, me, gathering.getHost(), reviewed));
                });

        // PARTNER 매칭 - IN_PROGRESS
        matchResultRepository.findActiveByUserId(me.getId(), MatchResult.MatchResultStatus.IN_PROGRESS)
                .forEach(matchResult -> {
                    UserProfile partner = matchResult.getUserA().getId().equals(me.getId())
                            ? matchResult.getUserB()
                            : matchResult.getUserA();

                    ChatRoom chatRoom = chatRoomRepository
                            .findByMatchResult_Id(matchResult.getId())
                            .orElse(null);

                    boolean reviewed = reviewRepository
                            .existsByReviewer_IdAndMatchResultId(me.getId(), matchResult.getId());

                    result.add(ActiveMatchResponse.ofPartner(matchResult, me, partner, chatRoom, reviewed));
                });

        return result;
    }

    @Transactional(readOnly = true)
    public List<MatchHistoryResponse> getMatchHistory(Long userId) {
        UserProfile me = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        List<MatchHistoryResponse> result = new ArrayList<>();

        // 1. 퀵매칭 완료 내역
        matchResultRepository.findActiveByUserId(me.getId(), MatchResult.MatchResultStatus.COMPLETED)
                .forEach(matchResult -> {
                    UserProfile partner = matchResult.getUserA().getId().equals(me.getId())
                            ? matchResult.getUserB() : matchResult.getUserA();

                    boolean reviewed = reviewRepository
                            .existsByReviewer_IdAndMatchResultId(me.getId(), matchResult.getId());

                    result.add(MatchHistoryResponse.builder()
                            .id(matchResult.getId())
                            .type("PARTNER")
                            .status("COMPLETED")
                            .partnerName(partner.getName())
                            .partnerProfileImageUrl(partner.getProfileImageUrl())
                            .partnerDepartment(partner.getDepartment())
                            .partnerStudentNumber(partner.getStudentId())
                            .matchedAt(matchResult.getCreatedAt() != null ?
                                matchResult.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).format(FORMATTER) : null)
                            .reviewed(reviewed)
                            .build());
                });

        // 2. 일반 모집 완료 내역 (방장)
        gatheringRepository.findByHost_IdAndStatus(me.getId(), Gathering.Status.COMPLETED)
                .forEach(gathering -> {
                    boolean reviewed = reviewRepository
                            .existsByReviewer_IdAndGatheringId(me.getId(), gathering.getId());

                    result.add(MatchHistoryResponse.builder()
                            .id(gathering.getId())
                            .type("GATHERING")
                            .status("COMPLETED")
                            .role("HOST")
                            .location(gathering.getVenue())
                            .sportType(gathering.getSportType())
                            .scheduledAt(gathering.getScheduledAt() != null ?
                                gathering.getScheduledAt().atZone(ZoneId.of("Asia/Seoul")).format(FORMATTER) : null)
                            .reviewed(reviewed)
                            .build());
                });

        // 3. 일반 모집 완료 내역 (참여자, 본인이 HOST인 경우 제외)
        gatheringParticipantRepository.findByUser_IdAndStatus(me.getId(), GatheringParticipant.Status.ACCEPTED)
                .stream()
                .filter(p -> p.getGathering().getStatus() == Gathering.Status.COMPLETED)
                .filter(p -> !p.getGathering().getHost().getId().equals(me.getId()))
                .forEach(participant -> {
                    Gathering gathering = participant.getGathering();
                    UserProfile host = gathering.getHost();
                    boolean reviewed = reviewRepository
                            .existsByReviewer_IdAndGatheringId(me.getId(), gathering.getId());

                    result.add(MatchHistoryResponse.builder()
                            .id(gathering.getId())
                            .type("GATHERING")
                            .status("COMPLETED")
                            .role("GUEST")
                            .partnerName(host.getName())
                            .partnerProfileImageUrl(host.getProfileImageUrl())
                            .partnerDepartment(host.getDepartment())
                            .partnerStudentNumber(host.getStudentId())
                            .location(gathering.getVenue())
                            .sportType(gathering.getSportType())
                            .scheduledAt(gathering.getScheduledAt() != null ?
                                gathering.getScheduledAt().atZone(ZoneId.of("Asia/Seoul")).format(FORMATTER) : null)
                            .reviewed(reviewed)
                            .build());
                });

        // 방어적 중복 제거 (id + type 기준)
        return result.stream()
                .collect(Collectors.toMap(
                        r -> r.getType() + "-" + r.getId(),
                        r -> r,
                        (a, b) -> a
                ))
                .values().stream()
                .toList();
    }
}