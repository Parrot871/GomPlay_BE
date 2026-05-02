package com.example.gomplay.domain.team.dto;

import com.example.gomplay.domain.team.entity.Gathering;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class GatheringListResponse {
    private Long id;
    private String title;
    private String sportType;
    private String difficulty;
    private String venue;
    private LocalDateTime scheduledAt;
    private LocalDateTime scheduledEndAt;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String tags;
    private String status;
    private String hostProfileImageUrl;
    private BigDecimal hostMannerTemperature;

    public GatheringListResponse(Gathering gathering) {
        this.id = gathering.getId();
        this.title = gathering.getTitle();
        this.sportType = gathering.getSportType();
        this.difficulty = gathering.getDifficulty();
        this.venue = gathering.getVenue();
        this.scheduledAt = gathering.getScheduledAt();
        this.scheduledEndAt = gathering.getScheduledEndAt();
        this.maxParticipants = gathering.getMaxParticipants();
        this.currentParticipants = gathering.getCurrentParticipants();
        this.tags = gathering.getTags();
        this.status = gathering.getStatus().name();
        this.hostProfileImageUrl = gathering.getHost().getProfileImageUrl();
        this.hostMannerTemperature = gathering.getHost().getMannerTemperature();
    }
}