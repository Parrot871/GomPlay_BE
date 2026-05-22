package com.example.gomplay.domain.matching.repository;

import com.example.gomplay.domain.matching.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    Optional<MatchResult> findByMatchRequest_Id(Long matchRequestId);
    Optional<MatchResult> findByUserA_IdOrUserB_IdAndStatus(
            Long userAId, Long userBId, MatchResult.MatchResultStatus status
    );

    // 추가
    @Query("SELECT m FROM MatchResult m WHERE (m.userA.id = :userId OR m.userB.id = :userId) AND m.status = :status")
    List<MatchResult> findActiveByUserId(@Param("userId") Long userId, @Param("status") MatchResult.MatchResultStatus status);
}