package com.example.gomplay.domain.groupchat.dto;

import com.example.gomplay.domain.groupchat.entity.GroupChatRoom;
import lombok.Getter;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
public class GroupChatRoomResponse {
    private Long id;
    private Long gatheringId;
    private String gatheringTitle;
    private String sportType;
    private int participantCount;
    private GroupChatMessageDto lastMessage;
    private String createdAt;
    private String hostProfileImageUrl;

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
            .withZone(ZoneId.of("Asia/Seoul"));

    public static GroupChatRoomResponse of(GroupChatRoom room, GroupChatMessageDto lastMessage, int participantCount) {
        GroupChatRoomResponse dto = new GroupChatRoomResponse();
        dto.id = room.getId();
        dto.gatheringId = room.getGathering().getId();
        dto.gatheringTitle = room.getGathering().getTitle();
        dto.sportType = room.getGathering().getSportType();
        dto.participantCount = participantCount;
        dto.lastMessage = lastMessage;
        dto.createdAt = room.getCreatedAt() != null ?
            room.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).format(FORMATTER) : null;
        dto.hostProfileImageUrl = room.getGathering().getHost().getProfileImageUrl();
        return dto;
    }
}