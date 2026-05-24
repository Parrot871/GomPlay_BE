package com.example.gomplay.domain.team.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class GatheringUpdateRequest {
    private String title;
    private String sportType;
    private String difficulty;
    private String venue;
    private BigDecimal venueLat;
    private BigDecimal venueLng;
    private LocalDateTime scheduledAt;
    private LocalDateTime scheduledEndAt;
    private Integer maxParticipants;
    private String description;
    private String tags;
    private String status;
    private String openChatUrl;
}