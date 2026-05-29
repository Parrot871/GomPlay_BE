package com.example.gomplay.domain.groupchat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;

@Getter
public class GroupChatParticipantDto {
    private Long id;
    private String name;
    private String profileImageUrl;
    @JsonIgnore
    private boolean isHost;
    @JsonProperty("isHost")
    public boolean getIsHost() {
        return isHost;
    }

    public static GroupChatParticipantDto of(Long id, String name, String profileImageUrl, boolean isHost) {
        GroupChatParticipantDto dto = new GroupChatParticipantDto();
        dto.id = id;
        dto.name = name;
        dto.profileImageUrl = profileImageUrl;
        dto.isHost = isHost;
        return dto;
    }
}