package com.example.gomplay.domain.matching.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MatchRequestDto {
    private Long opponentId; // 요청 보낼 상대방 userProfileId
}
