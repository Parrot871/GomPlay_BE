package com.example.gomplay.domain.groupchat.repository;

import com.example.gomplay.domain.groupchat.entity.GroupChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GroupChatRoomRepository extends JpaRepository<GroupChatRoom, Long> {
    Optional<GroupChatRoom> findByGathering_Id(Long gatheringId);
}