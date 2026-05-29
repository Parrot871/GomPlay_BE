package com.example.gomplay.domain.groupchat.dto;

import com.example.gomplay.domain.groupchat.entity.GroupChatRoom;
import com.example.gomplay.domain.team.entity.Gathering;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private boolean isHost;
    @JsonProperty("isHost")
    public boolean getIsHost() {
    return isHost;
    }
    private String gatheringStatus;
    private boolean reviewed;
    private List<GroupChatParticipantDto> participants;
    private List<GroupChatMessageDto> messages;

    public static GroupChatRoomDetailResponse of(
            GroupChatRoom room,
            List<GroupChatMessageDto> messages,
            int participantCount,
            boolean isHost,
            boolean reviewed,
            List<GroupChatParticipantDto> participants) {

        GroupChatRoomDetailResponse dto = new GroupChatRoomDetailResponse();
        dto.id = room.getId();
        dto.gatheringId = room.getGathering().getId();
        dto.gatheringTitle = room.getGathering().getTitle();
        dto.sportType = room.getGathering().getSportType();
        dto.venue = room.getGathering().getVenue();
        dto.participantCount = participantCount;
        dto.isHost = isHost;
        dto.gatheringStatus = toGatheringStatus(room.getGathering().getStatus());
        dto.reviewed = reviewed;
        dto.participants = participants;
        dto.messages = messages;
        return dto;
    }

    private static String toGatheringStatus(Gathering.Status status) {
        return switch (status) {
            case COMPLETED -> "COMPLETED";
            default -> "IN_PROGRESS";
        };
    }
}