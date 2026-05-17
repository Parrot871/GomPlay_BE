package com.example.gomplay.domain.chat.repository;

import com.example.gomplay.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 채팅방 메시지 전체 조회 (시간순)
    List<ChatMessage> findByRoom_IdOrderBySentAtAsc(Long roomId);

    // 안 읽은 메시지 읽음 처리
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.room.id = :roomId AND m.sender.id != :userId AND m.isRead = false")
    void markAsRead(@Param("roomId") Long roomId, @Param("userId") Long userId);

    // 안 읽은 메시지 수
    long countByRoom_IdAndIsReadFalseAndSender_IdNot(Long roomId, Long senderId);
}
