package com.example.gomplay.domain.team.dto;

import com.example.gomplay.domain.team.entity.Gathering;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class GatheringHistoryResponse {
    private Long id;
    private String title;
    private String sportType;
    private String venue;
    private LocalDateTime scheduledAt;
    private LocalDateTime scheduledEndAt;
    private boolean isReviewed;

    public GatheringHistoryResponse(Gathering gathering, boolean isReviewed) {
        this.id = gathering.getId();
        this.title = gathering.getTitle();
        this.sportType = gathering.getSportType();
        this.venue = gathering.getVenue();
        this.scheduledAt = gathering.getScheduledAt();
        this.scheduledEndAt = gathering.getScheduledEndAt();
        this.isReviewed = isReviewed;
    }
}