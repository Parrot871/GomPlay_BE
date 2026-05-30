package com.example.gomplay.domain.team.dto;

import com.example.gomplay.domain.team.entity.Gathering;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class GatheringCreateResponse {
    private Long id;
    private String title;
    private String sportType;
    private String difficulty;
    private String venue;
    private LocalDateTime scheduledAt;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String status;
    private LocalDateTime createdAt;
    private String description;
    private LocalDateTime scheduledEndAt;
    private String tags;

    public GatheringCreateResponse(Gathering gathering) {
        this.id = gathering.getId();
        this.title = gathering.getTitle();
        this.sportType = gathering.getSportType();
        this.difficulty = gathering.getDifficulty();
        this.venue = gathering.getVenue();
        this.scheduledAt = gathering.getScheduledAt();
        this.maxParticipants = gathering.getMaxParticipants();
        this.currentParticipants = gathering.getCurrentParticipants();
        this.status = gathering.getStatus().name();
        this.createdAt = gathering.getCreatedAt();
    }
}