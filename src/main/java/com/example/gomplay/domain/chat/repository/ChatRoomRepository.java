package com.example.gomplay.domain.chat.repository;

import com.example.gomplay.domain.chat.entity.ChatRoom;
import com.example.gomplay.domain.matching.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByMatchResult_Id(Long matchResultId);

    // 12시간 지난 채팅방 조회 (스케줄러용)
    List<ChatRoom> findByCreatedAtBeforeAndMatchResult_Status(
            LocalDateTime dateTime,
            MatchResult.MatchResultStatus status
    );

    // 내가 참여한 채팅방 조회
    List<ChatRoom> findByUserA_IdOrUserB_Id(Long userAId, Long userBId);
}
