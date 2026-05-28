package com.example.gomplay.domain.groupchat.dto;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class GroupChatScheduleRequest {
    private String content;
    private LocalDateTime scheduledAt;
    private String venue;
    private String sportType;
}