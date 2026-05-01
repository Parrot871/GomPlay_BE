package com.example.gomplay.domain.chat.repository;

import com.example.gomplay.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 특정 유저의 채팅방 목록 조회
    @Query("SELECT c FROM ChatRoom c WHERE c.userA.id = :userId OR c.userB.id = :userId")
    List<ChatRoom> findByUserId(@Param("userId") Long userId);

    // 두 유저 사이의 채팅방 조회
    @Query("SELECT c FROM ChatRoom c WHERE (c.userA.id = :userAId AND c.userB.id = :userBId) OR (c.userA.id = :userBId AND c.userB.id = :userAId)")
    Optional<ChatRoom> findByUserAIdAndUserBId(@Param("userAId") Long userAId, @Param("userBId") Long userBId);
}