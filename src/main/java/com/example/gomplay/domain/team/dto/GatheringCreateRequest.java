package com.example.gomplay.domain.team.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class GatheringCreateRequest {
    private String title;
    private String sportType;
    private String difficulty;
    private String venue;
    private BigDecimal venueLat;
    private BigDecimal venueLng;
    private LocalDateTime scheduledAt;
    private Integer maxParticipants;
}