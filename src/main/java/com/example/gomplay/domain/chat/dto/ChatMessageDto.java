package com.example.gomplay.domain.chat.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessageDto {

    public enum MessageType {
        ENTER, TALK, LEAVE
    }

    private MessageType type;      // 메시지 타입
    private Long roomId;           // 채팅방 ID
    private Long senderId;         // 보낸 사람 ID
    private String senderName;     // 보낸 사람 이름
    private String content;        // 메시지 내용
    private LocalDateTime sentAt;  // 전송 시간
}