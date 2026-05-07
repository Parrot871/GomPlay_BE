package com.example.gomplay.domain.team.dto;

import com.example.gomplay.domain.team.entity.GatheringParticipant;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class GatheringParticipantResponse {
    private Long id;
    private Long gatheringId;
    private Long userId;
    private String status;
    private LocalDateTime updatedAt;

    public GatheringParticipantResponse(GatheringParticipant participant) {
        this.id = participant.getId();
        this.gatheringId = participant.getGathering().getId();
        this.userId = participant.getUser().getId();
        this.status = participant.getStatus().name();
        this.updatedAt = participant.getUpdatedAt();
    }
}