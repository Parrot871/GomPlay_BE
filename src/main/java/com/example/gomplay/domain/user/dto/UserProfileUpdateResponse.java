package com.example.gomplay.domain.user.dto;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class UserProfileUpdateResponse {
    private Long userId;
    private LocalDateTime updatedAt;

    public UserProfileUpdateResponse(Long userId, LocalDateTime updatedAt) {
        this.userId = userId;
        this.updatedAt = updatedAt;
    }
}