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

import java.util.stream.Collectors;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.data.domain.Page;
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

    @Transactional
    public GatheringCreateResponse createGathering(Long userId, GatheringCreateRequest request) {
        UserProfile host = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

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
                request.getStatus()
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

        return new GatheringJoinResponse(gatheringParticipantRepository.save(participant));
    }

    @Transactional(readOnly = true)
    public Page<GatheringListResponse> getGatheringList(
        String sportType, String difficulty, String status, int page, int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("scheduledAt").ascending());

    Gathering.Status gatheringStatus = status != null ? Gathering.Status.valueOf(status) : null;

    if (sportType != null && difficulty != null && gatheringStatus != null) {
        return gatheringRepository.findBySportTypeAndDifficultyAndStatus(sportType, difficulty, gatheringStatus, pageable)
                .map(GatheringListResponse::new);
    } else if (sportType != null && gatheringStatus != null) {
        return gatheringRepository.findBySportTypeAndStatus(sportType, gatheringStatus, pageable)
                .map(GatheringListResponse::new);
    } else if (difficulty != null && gatheringStatus != null) {
        return gatheringRepository.findByDifficultyAndStatus(difficulty, gatheringStatus, pageable)
                .map(GatheringListResponse::new);
    } else if (gatheringStatus != null) {
        return gatheringRepository.findByStatus(gatheringStatus, pageable)
                .map(GatheringListResponse::new);
    } else if (sportType != null && difficulty != null) {
        return gatheringRepository.findBySportTypeAndDifficulty(sportType, difficulty, pageable)
                .map(GatheringListResponse::new);
    } else if (sportType != null) {
        return gatheringRepository.findBySportType(sportType, pageable)
                .map(GatheringListResponse::new);
    } else if (difficulty != null) {
        return gatheringRepository.findByDifficulty(difficulty, pageable)
                .map(GatheringListResponse::new);
    } else {
        return gatheringRepository.findAll(pageable)
                .map(GatheringListResponse::new);
        }
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