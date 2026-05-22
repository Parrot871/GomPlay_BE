package com.example.gomplay.domain.chat.dto;

import com.example.gomplay.domain.chat.entity.ChatRoom;
import com.example.gomplay.domain.matching.entity.MatchResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDetailResponse {
    private Long roomId;
    private Long matchResultId;
    private Long opponentId;
    private String opponentName;
    private String opponentProfileImageUrl;
    private String matchStatus;
    private long unreadCount;
    private boolean isCompleteButtonVisible;
    private boolean reviewed;
    private LocalDateTime createdAt;
    private List<ChatMessageDto> messages;  // 전체 메시지

    public static ChatRoomDetailResponse of(ChatRoom room, Long myId, List<ChatMessageDto> messages, long unreadCount, boolean reviewed) {
        MatchResult matchResult = room.getMatchResult();

        boolean iAmA = room.getUserA().getId().equals(myId);
        var opponent = iAmA ? room.getUserB() : room.getUserA();

        boolean isCompleteButtonVisible = room.getCreatedAt()
                .plusMinutes(1)
                .isBefore(LocalDateTime.now())
                && matchResult.getStatus() == MatchResult.MatchResultStatus.IN_PROGRESS;

        return ChatRoomDetailResponse.builder()
                .roomId(room.getId())
                .matchResultId(matchResult.getId())
                .opponentId(opponent.getId())
                .opponentName(opponent.getName())
                .opponentProfileImageUrl(opponent.getProfileImageUrl())
                .matchStatus(matchResult.getStatus().name())
                .unreadCount(unreadCount)
                .isCompleteButtonVisible(isCompleteButtonVisible)
                .reviewed(reviewed)
                .createdAt(room.getCreatedAt())
                .messages(messages)
                .build();
    }
}