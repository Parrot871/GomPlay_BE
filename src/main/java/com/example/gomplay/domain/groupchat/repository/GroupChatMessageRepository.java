package com.example.gomplay.domain.groupchat.repository;

import com.example.gomplay.domain.groupchat.entity.GroupChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GroupChatMessageRepository extends JpaRepository<GroupChatMessage, Long> {
    List<GroupChatMessage> findByRoom_IdOrderBySentAtAsc(Long roomId);
    Optional<GroupChatMessage> findTopByRoom_IdOrderBySentAtDesc(Long roomId);
}