package com.example.gomplay.domain.matching.repository;

import com.example.gomplay.domain.matching.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    Optional<MatchResult> findByMatchRequest_Id(Long matchRequestId);
    Optional<MatchResult> findByUserA_IdOrUserB_IdAndStatus(
            Long userAId, Long userBId, MatchResult.MatchResultStatus status
    );
}
