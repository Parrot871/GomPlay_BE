package com.example.gomplay.domain.chat.repository;

import com.example.gomplay.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 채팅방 메시지 조회 (시간순)
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);

    // 안 읽은 메시지 수 조회
    int countByChatRoomIdAndIsReadFalseAndSenderIdNot(Long chatRoomId, Long senderId);
}