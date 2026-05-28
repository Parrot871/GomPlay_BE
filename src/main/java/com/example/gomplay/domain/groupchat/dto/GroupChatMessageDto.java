package com.example.gomplay.domain.groupchat.dto;

import com.example.gomplay.domain.groupchat.entity.GroupChatMessage;
import lombok.Getter;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
public class GroupChatMessageDto {
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderProfileImageUrl;
    private String content;
    private String messageType;
    private String scheduledAt;
    private String venue;
    private String sportType;
    private String sentAt;

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
            .withZone(ZoneId.of("Asia/Seoul"));

    public static GroupChatMessageDto of(GroupChatMessage message) {
        GroupChatMessageDto dto = new GroupChatMessageDto();
        dto.id = message.getId();
        dto.senderId = message.getSender().getId();
        dto.senderName = message.getSender().getName();
        dto.senderProfileImageUrl = message.getSender().getProfileImageUrl();
        dto.content = message.getContent();
        dto.messageType = message.getMessageType().name();
        dto.scheduledAt = message.getScheduledAt() != null ?
            message.getScheduledAt().atZone(ZoneId.of("Asia/Seoul")).format(FORMATTER) : null;
        dto.venue = message.getVenue();
        dto.sportType = message.getSportType();
        dto.sentAt = message.getSentAt() != null ?
            message.getSentAt().atZone(ZoneId.of("Asia/Seoul")).format(FORMATTER) : null;
        return dto;
    }
}