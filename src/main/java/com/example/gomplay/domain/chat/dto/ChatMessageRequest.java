package com.example.gomplay.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageRequest {
    private Long roomId;
    private Long senderId;
    private String content;
}