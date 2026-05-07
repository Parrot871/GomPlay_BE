package com.example.gomplay.domain.matching.repository;

import com.example.gomplay.domain.matching.entity.MatchRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {

    // 상대방한테 이미 PENDING 요청이 있는지 확인
    Optional<MatchRequest> findByRequester_IdAndOpponent_IdAndStatus(
            Long requesterId, Long opponentId, MatchRequest.MatchRequestStatus status
    );

    List<MatchRequest> findByStatusAndExpiresAtBefore(
            MatchRequest.MatchRequestStatus status,
            LocalDateTime dateTime
    );
}