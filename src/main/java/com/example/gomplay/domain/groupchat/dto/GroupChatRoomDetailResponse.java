package com.example.gomplay.domain.groupchat.dto;

import com.example.gomplay.domain.groupchat.entity.GroupChatRoom;
import lombok.Getter;
import java.util.List;

@Getter
public class GroupChatRoomDetailResponse {
    private Long id;
    private Long gatheringId;
    private String gatheringTitle;
    private String sportType;
    private String venue;
    private int participantCount;
    private List<GroupChatMessageDto> messages;

    public static GroupChatRoomDetailResponse of(GroupChatRoom room, List<GroupChatMessageDto> messages, int participantCount) {
        GroupChatRoomDetailResponse dto = new GroupChatRoomDetailResponse();
        dto.id = room.getId();
        dto.gatheringId = room.getGathering().getId();
        dto.gatheringTitle = room.getGathering().getTitle();
        dto.sportType = room.getGathering().getSportType();
        dto.venue = room.getGathering().getVenue();
        dto.participantCount = participantCount;
        dto.messages = messages;
        return dto;
    }
}