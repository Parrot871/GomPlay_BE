package com.example.gomplay.domain.chat.dto;

import com.example.gomplay.domain.chat.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String content;
    private boolean isRead;
    private LocalDateTime sentAt;

    public static ChatMessageDto of(ChatMessage message) {
        return ChatMessageDto.builder()
                .messageId(message.getId())
                .roomId(message.getRoom().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getName())
                .content(message.getContent())
                .isRead(message.isRead())
                .sentAt(message.getSentAt())
                .build();
    }
}
