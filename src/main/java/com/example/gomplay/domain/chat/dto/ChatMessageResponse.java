package com.example.gomplay.domain.chat.dto;

import com.example.gomplay.domain.chat.entity.ChatMessage;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ChatMessageResponse {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String content;
    private boolean isRead;
    private LocalDateTime sentAt;

    public ChatMessageResponse(ChatMessage message) {
        this.id = message.getId();
        this.roomId = message.getChatRoom().getId();
        this.senderId = message.getSender().getId();
        this.senderName = message.getSender().getName();
        this.content = message.getContent();
        this.isRead = message.isRead();
        this.sentAt = message.getSentAt();
    }
}