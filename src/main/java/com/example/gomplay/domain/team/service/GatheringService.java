package com.example.gomplay.domain.team.service;

import com.example.gomplay.domain.team.dto.GatheringUpdateRequest;
import com.example.gomplay.domain.team.dto.GatheringUpdateResponse;
import com.example.gomplay.domain.auth.entity.AuthUser;
import com.example.gomplay.domain.auth.repository.AuthUserRepository;
import com.example.gomplay.domain.point.service.PointService;
import com.example.gomplay.domain.review.repository.ReviewRepository;
import com.example.gomplay.domain.team.dto.GatheringCreateRequest;
import com.example.gomplay.domain.team.dto.GatheringCreateResponse;
import com.example.gomplay.domain.team.entity.Gathering;
import com.example.gomplay.domain.team.repository.GatheringRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import com.example.gomplay.domain.team.dto.GatheringJoinResponse;
import com.example.gomplay.domain.team.dto.GatheringListResponse;
import com.example.gomplay.domain.team.dto.GatheringParticipantResponse;
import com.example.gomplay.domain.team.entity.GatheringParticipant;
import com.example.gomplay.domain.team.repository.GatheringParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.gomplay.domain.team.dto.GatheringDetailResponse;
import com.example.gomplay.domain.team.dto.GatheringHistoryResponse;
import com.example.gomplay.domain.point.repository.PointLogRepository;
import com.example.gomplay.domain.notification.entity.Notification;
import com.example.gomplay.domain.notification.service.NotificationService;


import java.util.stream.Collectors;
import java.util.List;
import java.util.Random;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;


@Service
@RequiredArgsConstructor
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final UserProfileRepository userProfileRepository;
    private final GatheringParticipantRepository gatheringParticipantRepository;
    private final PointService pointService;
    private final ReviewRepository reviewRepository;
    private final PointLogRepository pointLogRepository;
    private final NotificationService notificationService;

    @Transactional
    public GatheringCreateResponse createGathering(Long userId, GatheringCreateRequest request) {
        UserProfile host = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        if (request.getOpenChatUrl() == null || !request.getOpenChatUrl().startsWith("https://open.kakao.com/o/")) {
                throw new IllegalArgumentException("카카오 오픈채팅 링크를 입력해주세요. (https://open.kakao.com/o/로 시작해야 합니다.)");
        }


        Gathering gathering = Gathering.builder()
                .host(host)
                .title(request.getTitle())
                .sportType(request.getSportType())
                .difficulty(request.getDifficulty())
                .venue(request.getVenue())
                .venueLat(request.getVenueLat())
                .venueLng(request.getVenueLng())
                .scheduledAt(request.getScheduledAt())
                .scheduledEndAt(request.getScheduledEndAt())
                .maxParticipants(request.getMaxParticipants())
                .description(request.getDescription())
                .tags(request.getTags())
                .openChatUrl(request.getOpenChatUrl())
                .build();

        Gathering saved = gatheringRepository.save(gathering);

        // 모집글 등록 포인트 지급 +2P
        pointService.addPoint(host, 2, "gathering", saved.getId());

        return new GatheringCreateResponse(saved);
    }

    @Transactional
    public GatheringUpdateResponse updateGathering(Long userId, Long gatheringId, GatheringUpdateRequest request) {
        UserProfile host = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("모집글을 찾을 수 없습니다."));

        if (!gathering.getHost().getId().equals(host.getId())) {
            throw new IllegalArgumentException("모집글 작성자만 수정할 수 있습니다.");
        }

        gathering.update(
                request.getTitle(),
                request.getSportType(),
                request.getDifficulty(),
                request.getVenue(),
                request.getVenueLat(),
                request.getVenueLng(),
                request.getScheduledAt(),
                request.getScheduledEndAt(),
                request.getMaxParticipants(),
                request.getDescription(),
                request.getTags(),
                request.getStatus(),
                request.getOpenChatUrl()
        );

        return new GatheringUpdateResponse(gathering);
    }

    @Transactional
    public void deleteGathering(Long userId, Long gatheringId) {
        UserProfile host = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Gathering gathering = gatheringRepository.findById(gatheringId)
            .orElseThrow(() -> new IllegalArgumentException("모집글을 찾을 수 없습니다."));

    if (!gathering.getHost().getId().equals(host.getId())) {
        throw new IllegalArgumentException("모집글 작성자만 삭제할 수 있습니다.");
    }

        gatheringParticipantRepository.deleteAllByGathering_Id(gatheringId);
        pointLogRepository.deleteByGatheringId(gatheringId);

        gatheringRepository.delete(gathering);
        }

    @Transactional(readOnly = true)
    public GatheringDetailResponse getGathering(Long gatheringId) {
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("모집글을 찾을 수 없습니다."));
        return new GatheringDetailResponse(gathering);
    }

   @Transactional
   public GatheringJoinResponse joinGathering(Long userId, Long gatheringId) {
        UserProfile user = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("모집글을 찾을 수 없습니다."));

        if (gathering.getStatus() != Gathering.Status.OPEN) {
                throw new IllegalArgumentException("모집이 마감된 글입니다.");
        }

        if (gathering.getHost().getId().equals(user.getId())) {
                throw new IllegalArgumentException("본인이 작성한 모집글에는 신청할 수 없습니다.");
        }

        if (gatheringParticipantRepository.existsByGathering_IdAndUser_Id(gatheringId, user.getId())) {
                throw new IllegalArgumentException("이미 신청한 모집글입니다.");
        }

        GatheringParticipant participant = GatheringParticipant.builder()
                .gathering(gathering)
                .user(user)
                .build();

        GatheringParticipant saved = gatheringParticipantRepository.save(participant);

        // 호스트에게 알림
        notificationService.createNotification(
                gathering.getHost(),
                Notification.NotificationType.gathering_request,
                "새로운 참여 신청",
                user.getName() + "님이 '" + gathering.getTitle() + "' 모집글에 참여 신청했어요!",
                gathering.getId()
        );

        return new GatheringJoinResponse(saved);
  }

    @Transactional(readOnly = true)
    public Page<GatheringListResponse> getGatheringList(
    String sportType, String difficulty, String status, boolean hideExpired, int page, int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by("scheduledAt").descending());

    Gathering.Status gatheringStatus = status != null ? Gathering.Status.valueOf(status) : null;

    Page<Gathering> gatherings;

        if (sportType != null && difficulty != null && gatheringStatus != null) {
                gatherings = gatheringRepository.findBySportTypeAndDifficultyAndStatus(sportType, difficulty, gatheringStatus, pageable);
        } else if (sportType != null && gatheringStatus != null) {
                gatherings = gatheringRepository.findBySportTypeAndStatus(sportType, gatheringStatus, pageable);
        } else if (difficulty != null && gatheringStatus != null) {
                gatherings = gatheringRepository.findByDifficultyAndStatus(difficulty, gatheringStatus, pageable);
        } else if (gatheringStatus != null) {
                gatherings = gatheringRepository.findByStatus(gatheringStatus, pageable);
        } else if (sportType != null && difficulty != null) {
                gatherings = gatheringRepository.findBySportTypeAndDifficulty(sportType, difficulty, pageable);
        } else if (sportType != null) {
                gatherings = gatheringRepository.findBySportType(sportType, pageable);
        } else if (difficulty != null) {
                gatherings = gatheringRepository.findByDifficulty(difficulty, pageable);
        } else {
                gatherings = gatheringRepository.findAll(pageable);
        }

        List<Gathering> content = new ArrayList<>(gatherings.getContent());
        LocalDateTime now = LocalDateTime.now();

        long seed = now.getYear() * 100000000L +
                now.getMonthValue() * 1000000L +
                now.getDayOfMonth() * 10000L +
                now.getHour() * 100L +
                (now.getMinute() / 10) * 10L;
        Random random = new Random(seed);

        List<Gathering> boosted = content.stream()
                .filter(g -> g.isBoosted() && g.getBoostExpiredAt() != null && g.getBoostExpiredAt().isAfter(now))
                .collect(Collectors.toList());

        List<Gathering> normal = content.stream()
                .filter(g -> !g.isBoosted() || g.getBoostExpiredAt() == null || g.getBoostExpiredAt().isBefore(now))
                .collect(Collectors.toList());

        List<Gathering> result = new ArrayList<>();
        if (!boosted.isEmpty()) {
                Collections.shuffle(boosted, random);
                result.add(boosted.get(0));
        }

        // 만료된 모집글 필터링
        if (hideExpired) {
        normal = normal.stream()
                .filter(g -> g.getScheduledAt() == null || g.getScheduledAt().isAfter(now))
                .collect(Collectors.toList());
        }
        result.addAll(normal);

        return new PageImpl<>(
                result.stream().map(GatheringListResponse::new).collect(Collectors.toList()),
                gatherings.getPageable(),
                gatherings.getTotalElements()
        );
        }



        @Transactional
        public GatheringParticipantResponse acceptParticipant(Long userId, Long gatheringId, Long participantId) {
        UserProfile host = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("모집글을 찾을 수 없습니다."));

        if (!gathering.getHost().getId().equals(host.getId())) {
                throw new IllegalArgumentException("모집글 작성자만 수락할 수 있습니다.");
        }

        GatheringParticipant participant = gatheringParticipantRepository
                .findByIdAndGathering_Id(participantId, gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보를 찾을 수 없습니다."));

        participant.updateStatus(GatheringParticipant.Status.ACCEPTED);

        //현재 참여자 수 증가
        gathering.incrementCurrentParticipants();

        // 인원 충족 시 CLOSED 처리
        long acceptedCount = gatheringParticipantRepository
                .countByGathering_IdAndStatus(gatheringId, GatheringParticipant.Status.ACCEPTED);

        if (acceptedCount >= gathering.getMaxParticipants()) {
                gathering.updateStatus(Gathering.Status.CLOSED);
        }

        // 신청자에게 알림
        notificationService.createNotification(
        participant.getUser(),
        Notification.NotificationType.gathering,
        "참여 신청 수락",
        "'" + gathering.getTitle() + "' 모집글 참여가 수락되었어요!",
        gathering.getId()
        );

        return new GatheringParticipantResponse(participant);
        }

    @Transactional
    public GatheringParticipantResponse rejectParticipant(Long userId, Long gatheringId, Long participantId) {
        UserProfile host = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("모집글을 찾을 수 없습니다."));

        if (!gathering.getHost().getId().equals(host.getId())) {
            throw new IllegalArgumentException("모집글 작성자만 거절할 수 있습니다.");
        }

        GatheringParticipant participant = gatheringParticipantRepository
                .findByIdAndGathering_Id(participantId, gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보를 찾을 수 없습니다."));

        participant.updateStatus(GatheringParticipant.Status.REJECTED);

        // 신청자에게 알림
        notificationService.createNotification(
        participant.getUser(),
        Notification.NotificationType.gathering,
        "참여 신청 거절",
        "'" + gathering.getTitle() + "' 모집글 참여가 거절되었어요.",
        gathering.getId()
        );

        return new GatheringParticipantResponse(participant);
    }

    @Transactional
    public void completeGathering(Long userId, Long gatheringId) {
        UserProfile user = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("모집글을 찾을 수 없습니다."));

        // 방장인 경우
        if (gathering.getHost().getId().equals(user.getId())) {
                gathering.completeByHost();
        } else {
                // 참여자인 경우
                GatheringParticipant participant = gatheringParticipantRepository
                        .findByGathering_IdAndUser_Id(gatheringId, user.getId())
                        .orElseThrow(() -> new IllegalArgumentException("참여자를 찾을 수 없습니다."));
                participant.complete();
        }

        // 전원 완료 여부 체크 (방장 + 모든 참여자)
        boolean hostCompleted = gathering.isHostCompleted();
        boolean allParticipantsCompleted = gatheringParticipantRepository
                .findByGathering_Id(gatheringId)
                .stream()
                .allMatch(GatheringParticipant::isCompleted);

        if (hostCompleted && allParticipantsCompleted) {
                gathering.updateStatus(Gathering.Status.COMPLETED);

                // 운동 완료 포인트 지급 +4P
                pointService.addPoint(user, 4, "exercise_complete", null);

                // 전원 완료 시 리뷰 가능 알림 (방장 + 모든 참여자)
                List<GatheringParticipant> participants = gatheringParticipantRepository
                        .findByGathering_Id(gatheringId);

                // 방장에게 알림
                notificationService.createNotification(
                gathering.getHost(),
                Notification.NotificationType.review_available,
                "평가 가능",
                "'" + gathering.getTitle() + "' 운동이 완료됐어요! 상대방을 평가해보세요.",
                gatheringId
                );

                // 참여자들에게 알림
                participants.forEach(p -> notificationService.createNotification(
                p.getUser(),
                Notification.NotificationType.review_available,
                "평가 가능",
                "'" + gathering.getTitle() + "' 운동이 완료됐어요! 상대방을 평가해보세요.",
                gatheringId
                ));

        } else {
                // 한쪽만 완료 버튼 눌렀을 때 → match_end_confirm 알림
                // 방장이 눌렀으면 참여자들에게, 참여자가 눌렀으면 방장에게
                if (gathering.getHost().getId().equals(user.getId())) {
                // 참여자들에게 알림
                gatheringParticipantRepository.findByGathering_Id(gatheringId)
                        .forEach(p -> notificationService.createNotification(
                        p.getUser(),
                        Notification.NotificationType.match_end_confirm,
                        "매칭 종료 대기",
                        "'" + gathering.getTitle() + "' 방장이 운동 완료를 눌렀어요! 확인해주세요.",
                        gatheringId
                        ));
                } else {
                // 방장에게 알림
                notificationService.createNotification(
                        gathering.getHost(),
                        Notification.NotificationType.match_end_confirm,
                        "매칭 종료 대기",
                        user.getName() + "님이 운동 완료를 눌렀어요! 확인해주세요.",
                        gatheringId
                );
                }
        }

        // 개인 완료 시 +4p 지급
        pointService.addPoint(user, 4, "exercise_complete", null);
        }

        @Transactional(readOnly = true)
        public List<GatheringHistoryResponse> getGatheringHistory(Long userId) {
        UserProfile user = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 방장으로 참여한 COMPLETED 모집글
        List<Gathering> hostGatherings = gatheringRepository
                .findByHost_IdAndStatus(user.getId(), Gathering.Status.COMPLETED);

        // 참여자로 참여한 COMPLETED 모집글
        List<Gathering> participantGatherings = gatheringRepository
                .findByParticipantIdAndStatus(user.getId(), Gathering.Status.COMPLETED);

        // 합치기 (중복 제거)
        List<Gathering> allGatherings = new java.util.ArrayList<>(hostGatherings);
        allGatherings.addAll(participantGatherings);

        return allGatherings.stream()
                .map(gathering -> {
                        boolean isReviewed = reviewRepository
                                .existsByReviewer_IdAndGatheringId(user.getId(), gathering.getId());
                        return new GatheringHistoryResponse(gathering, isReviewed);
                })
            .   collect(Collectors.toList());
        }

        @Transactional
        public void boostGathering(Long userId, Long gatheringId) {
                UserProfile user = userProfileRepository.findByAuthUser_Id(userId)
            .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Gathering gathering = gatheringRepository.findById(gatheringId)
            .orElseThrow(() -> new IllegalArgumentException("모집글을 찾을 수 없습니다."));

        if (!gathering.getHost().getId().equals(user.getId())) {
        throw new IllegalArgumentException("모집글 작성자만 부스트할 수 있습니다.");
        }

        if (user.getPointBalance() < 25) {
        throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        // 포인트 차감 -25P
        pointService.addPoint(user, -25, "boost", gatheringId);

        // 부스트 설정 (24시간)
        gathering.boost(LocalDateTime.now().plusHours(24));
        }

        //모집글 신청자 목록 조회(방장만 가능)
        @Transactional(readOnly = true)
        public List<GatheringParticipantResponse> getParticipants(Long userId, Long gatheringId) {
                UserProfile host = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("모집글을 찾을 수 없습니다."));

        if (!gathering.getHost().getId().equals(host.getId())) {
        throw new IllegalArgumentException("모집글 작성자만 조회할 수 있습니다.");
        }

        return gatheringParticipantRepository.findByGathering_Id(gatheringId)
                .stream()
                .map(GatheringParticipantResponse::new)
                .collect(Collectors.toList());
        }
}