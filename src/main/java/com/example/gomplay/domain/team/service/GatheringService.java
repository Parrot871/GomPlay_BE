package com.example.gomplay.domain.team.service;

import com.example.gomplay.domain.auth.entity.AuthUser;
import com.example.gomplay.domain.auth.repository.AuthUserRepository;
import com.example.gomplay.domain.team.dto.GatheringCreateRequest;
import com.example.gomplay.domain.team.dto.GatheringCreateResponse;
import com.example.gomplay.domain.team.entity.Gathering;
import com.example.gomplay.domain.team.repository.GatheringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final AuthUserRepository authUserRepository;

    @Transactional
    public GatheringCreateResponse createGathering(Long userId, GatheringCreateRequest request) {
        AuthUser host = authUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Gathering gathering = Gathering.builder()
                .host(host)
                .title(request.getTitle())
                .sportType(request.getSportType())
                .difficulty(Gathering.Difficulty.valueOf(request.getDifficulty()))
                .venue(request.getVenue())
                .venueLat(request.getVenueLat())
                .venueLng(request.getVenueLng())
                .scheduledAt(request.getScheduledAt())
                .maxParticipants(request.getMaxParticipants())
                .build();

        return new GatheringCreateResponse(gatheringRepository.save(gathering));
    }
}