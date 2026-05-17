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
    private String matchStatus;        // IN_PROGRESS or COMPLETED
    private boolean isCompleteButtonVisible; // 12시간 지났는지
    private LocalDateTime createdAt;
    private List<ChatMessageDto> messages;

    public static ChatRoomResponse of(ChatRoom room, Long myId, List<ChatMessageDto> messages) {
        MatchResult matchResult = room.getMatchResult();

        // 상대방 정보
        boolean iAmA = room.getUserA().getId().equals(myId);
        var opponent = iAmA ? room.getUserB() : room.getUserA();

        // 12시간 지났는지 체크
        boolean isCompleteButtonVisible = room.getCreatedAt()
                .plusHours(12)
                .isBefore(LocalDateTime.now())
                && matchResult.getStatus() == MatchResult.MatchResultStatus.IN_PROGRESS;

        return ChatRoomResponse.builder()
                .roomId(room.getId())
                .matchResultId(matchResult.getId())
                .opponentId(opponent.getId())
                .opponentName(opponent.getName())
                .opponentProfileImageUrl(opponent.getProfileImageUrl())
                .matchStatus(matchResult.getStatus().name())
                .isCompleteButtonVisible(isCompleteButtonVisible)
                .createdAt(room.getCreatedAt())
                .messages(messages)
                .build();
    }
}
