package com.example.gomplay.domain.matching.repository;

import com.example.gomplay.domain.matching.entity.QuickMatchLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuickMatchLogRepository extends JpaRepository<QuickMatchLog, Long> {
    Optional<QuickMatchLog> findTopByUserProfileIdAndStatus(
            Long userProfileId, QuickMatchLog.MatchStatus status
    );
    List<QuickMatchLog> findByStatus(QuickMatchLog.MatchStatus status);
    List<QuickMatchLog> findAllByUserProfileIdAndStatus(
            Long userProfileId, QuickMatchLog.MatchStatus status
    );
}
