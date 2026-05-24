package com.example.gomplay.domain.team.dto;

import com.example.gomplay.domain.team.entity.Gathering;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
public class GatheringListResponse {
    private Long id;
    private Long hostId;
    private String title;
    private String sportType;
    private String difficulty;
    private String venue;
    private String scheduledAt;
    private String scheduledEndAt;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String tags;
    private String status;
    private String hostProfileImageUrl;
    private BigDecimal hostMannerTemperature;
    @JsonProperty("isBoosted")
    private boolean boostedFlag;
    private String boostExpiredAt;

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
            .withZone(ZoneId.of("Asia/Seoul"));

    public GatheringListResponse(Gathering gathering) {
        this.id = gathering.getId();
        this.hostId = gathering.getHost().getId();
        this.title = gathering.getTitle();
        this.sportType = gathering.getSportType();
        this.difficulty = gathering.getDifficulty();
        this.venue = gathering.getVenue();
        this.scheduledAt = gathering.getScheduledAt() != null ?
            gathering.getScheduledAt().atZone(ZoneId.of("Asia/Seoul")).format(FORMATTER) : null;
        this.scheduledEndAt = gathering.getScheduledEndAt() != null ?
            gathering.getScheduledEndAt().atZone(ZoneId.of("Asia/Seoul")).format(FORMATTER) : null;
        this.maxParticipants = gathering.getMaxParticipants();
        this.currentParticipants = gathering.getCurrentParticipants();
        this.tags = gathering.getTags();
        this.status = gathering.getStatus().name();
        this.hostProfileImageUrl = gathering.getHost().getProfileImageUrl();
        this.hostMannerTemperature = gathering.getHost().getMannerTemperature();
        this.boostedFlag = gathering.isBoosted();
        this.boostExpiredAt = gathering.getBoostExpiredAt() != null ?
            gathering.getBoostExpiredAt().atZone(ZoneId.of("Asia/Seoul")).format(FORMATTER) : null;
    }
}