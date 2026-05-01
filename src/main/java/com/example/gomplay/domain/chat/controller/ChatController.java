package com.example.gomplay.domain.chat.controller; // ← 변경!

import com.example.gomplay.domain.chat.dto.ChatMessageDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import java.time.LocalDateTime;

@Controller
public class ChatController {

    @MessageMapping("/chat/message")
    @SendTo("/topic/chat/room")
    public ChatMessageDto sendMessage(ChatMessageDto message) {

        message.setSentAt(LocalDateTime.now());

    if (ChatMessageDto.MessageType.ENTER.equals(message.getType())) {
    message.setContent(message.getSenderName() + "님이 매칭 채팅방에 입장하셨습니다! 반갑게 인사해주세요 ⚽🏃");
    }

        return message;
    }
}