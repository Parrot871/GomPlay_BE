package com.example.gomplay.domain.team.service;

import com.example.gomplay.domain.team.dto.GatheringUpdateRequest;
import com.example.gomplay.domain.team.dto.GatheringUpdateResponse;
import com.example.gomplay.domain.auth.entity.AuthUser;
import com.example.gomplay.domain.auth.repository.AuthUserRepository;
import com.example.gomplay.domain.point.service.PointService;
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

        // 모집글 등록 포인트 지급 +5P
        pointService.addPoint(host, 5, "gathering", saved.getId());

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
            String sportType, String difficulty, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("scheduledAt").ascending());

        if (sportType != null && difficulty != null) {
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
}