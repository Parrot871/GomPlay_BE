package com.example.gomplay.domain.team.dto;

import com.example.gomplay.domain.team.entity.Gathering;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
public class GatheringHistoryResponse {
    private Long id;
    private String title;
    private String sportType;
    private String venue;
    private String scheduledAt;
    private String scheduledEndAt;
    @JsonProperty("isReviewed")
    private boolean reviewed;

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
            .withZone(ZoneId.of("Asia/Seoul"));

    public GatheringHistoryResponse(Gathering gathering, boolean isReviewed) {
        this.id = gathering.getId();
        this.title = gathering.getTitle();
        this.sportType = gathering.getSportType();
        this.venue = gathering.getVenue();
        this.scheduledAt = gathering.getScheduledAt() != null ?
            gathering.getScheduledAt().atZone(ZoneId.of("Asia/Seoul")).format(FORMATTER) : null;
        this.scheduledEndAt = gathering.getScheduledEndAt() != null ?
            gathering.getScheduledEndAt().atZone(ZoneId.of("Asia/Seoul")).format(FORMATTER) : null;
        this.reviewed = isReviewed;
    }
}