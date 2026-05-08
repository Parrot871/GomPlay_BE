package com.example.gomplay.domain.matching.repository;

import com.example.gomplay.domain.matching.entity.MatchRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {

    // 이미 PENDING 요청 있는지 확인
    Optional<MatchRequest> findByRequester_IdAndOpponent_IdAndStatus(
            Long requesterId, Long opponentId, MatchRequest.MatchRequestStatus status
    );

    // 만료된 PENDING 요청 조회 (스케줄러용)
    List<MatchRequest> findByStatusAndExpiresAtBefore(
            MatchRequest.MatchRequestStatus status,
            LocalDateTime dateTime
    );

    // 동시성 처리용 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MatchRequest m WHERE m.id = :id")
    Optional<MatchRequest> findByIdWithLock(@Param("id") Long id);

    // 나 또는 상대방이 이미 ACCEPTED 됐는지 확인
    @Query("SELECT COUNT(m) > 0 FROM MatchRequest m WHERE " +
            "(m.requester.id = :userId OR m.opponent.id = :userId) " +
            "AND m.status = 'ACCEPTED'")
    boolean existsAcceptedMatchByUserId(@Param("userId") Long userId);
}