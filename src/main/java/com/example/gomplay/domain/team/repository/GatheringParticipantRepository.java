package com.example.gomplay.domain.team.repository;

import com.example.gomplay.domain.team.entity.GatheringParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface GatheringParticipantRepository extends JpaRepository<GatheringParticipant, Long> {
    boolean existsByGathering_IdAndUser_Id(Long gatheringId, Long userId);
    Optional<GatheringParticipant> findByIdAndGathering_Id(Long id, Long gatheringId);
    Optional<GatheringParticipant> findByGathering_IdAndUser_Id(Long gatheringId, Long userId);
    List<GatheringParticipant> findByGathering_Id(Long gatheringId);
}