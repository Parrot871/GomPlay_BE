package com.example.gomplay.domain.team.dto;

import com.example.gomplay.domain.team.entity.Gathering;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
public class GatheringDetailResponse {
    private Long id;
    private Long hostId;
    private String title;
    private String sportType;
    private String difficulty;
    private String venue;
    private BigDecimal venueLat;
    private BigDecimal venueLng;
    private String scheduledAt;
    private String scheduledEndAt;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String description;
    private String tags;
    private String status;
    private String createdAt;
    private String hostName;
    private String hostProfileImageUrl;

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
            .withZone(ZoneId.of("Asia/Seoul"));

    public GatheringDetailResponse(Gathering gathering) {
        this.id = gathering.getId();
        this.hostId = gathering.getHost().getId();
        this.title = gathering.getTitle();
        this.sportType = gathering.getSportType();
        this.difficulty = gathering.getDifficulty();
        this.venue = gathering.getVenue();
        this.venueLat = gathering.getVenueLat();
        this.venueLng = gathering.getVenueLng();
        this.scheduledAt = gathering.getScheduledAt() != null ?
            gathering.getScheduledAt().atZone(ZoneId.of("Asia/Seoul")).format(FORMATTER) : null;
        this.scheduledEndAt = gathering.getScheduledEndAt() != null ?
            gathering.getScheduledEndAt().atZone(ZoneId.of("Asia/Seoul")).format(FORMATTER) : null;
        this.maxParticipants = gathering.getMaxParticipants();
        this.currentParticipants = gathering.getCurrentParticipants();
        this.description = gathering.getDescription();
        this.tags = gathering.getTags();
        this.status = gathering.getStatus().name();
        this.createdAt = gathering.getCreatedAt() != null ?
            gathering.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).format(FORMATTER) : null;
        this.hostName = gathering.getHost().getName();
        this.hostProfileImageUrl = gathering.getHost().getProfileImageUrl();
    }
}