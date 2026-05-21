package com.example.gomplay.domain.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchRequestResponse {
    private Long matchRequestId;
    private Long opponentId;
    private String opponentName;
    private String opponentProfileImageUrl;
    private String status;
    private Instant expiresAt;
}
