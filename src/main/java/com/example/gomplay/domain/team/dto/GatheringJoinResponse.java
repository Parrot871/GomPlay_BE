package com.example.gomplay.domain.team.dto;

import com.example.gomplay.domain.team.entity.GatheringParticipant;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class GatheringJoinResponse {
    private Long id;
    private Long gatheringId;
    private Long userId;
    private String status;
    private LocalDateTime createdAt;

    public GatheringJoinResponse(GatheringParticipant participant) {
        this.id = participant.getId();
        this.gatheringId = participant.getGathering().getId();
        this.userId = participant.getUser().getId();
        this.status = participant.getStatus().name();
        this.createdAt = participant.getCreatedAt();
    }
}