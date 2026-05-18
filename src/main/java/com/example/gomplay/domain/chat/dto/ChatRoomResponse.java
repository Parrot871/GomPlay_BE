
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
public class ChatRoomResponse {
    private Long roomId;
    private Long matchResultId;
    private Long opponentId;
    private String opponentName;
    private String opponentProfileImageUrl;
    private String matchStatus;
    private long unreadCount;
    private boolean isCompleteButtonVisible;
    private LocalDateTime createdAt;
    private String lastMessageContent;
    private LocalDateTime lastMessageAt;

    public static ChatRoomResponse of(ChatRoom room, Long myId, ChatMessageDto lastMessage, long unreadCount) {
        MatchResult matchResult = room.getMatchResult();

        boolean iAmA = room.getUserA().getId().equals(myId);
        var opponent = iAmA ? room.getUserB() : room.getUserA();

        boolean isCompleteButtonVisible = room.getCreatedAt()
                .plusMinutes(1)  // 1분 → 12시간으로 수정도 같이
                .isBefore(LocalDateTime.now())
                && matchResult.getStatus() == MatchResult.MatchResultStatus.IN_PROGRESS;

        return ChatRoomResponse.builder()
                .roomId(room.getId())
                .matchResultId(matchResult.getId())
                .opponentId(opponent.getId())
                .opponentName(opponent.getName())
                .opponentProfileImageUrl(opponent.getProfileImageUrl())
                .matchStatus(matchResult.getStatus().name())
                .unreadCount(unreadCount)
                .isCompleteButtonVisible(isCompleteButtonVisible)
                .createdAt(room.getCreatedAt())
                .lastMessageContent(lastMessage != null ? lastMessage.getContent() : null)
                .lastMessageAt(lastMessage != null ? lastMessage.getSentAt() : null)
                .build();
    }
}

