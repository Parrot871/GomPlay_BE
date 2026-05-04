package com.example.gomplay.domain.team.repository;

import com.example.gomplay.domain.team.entity.GatheringParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringParticipantRepository extends JpaRepository<GatheringParticipant, Long> {
    boolean existsByGathering_IdAndUser_Id(Long gatheringId, Long userId);
}