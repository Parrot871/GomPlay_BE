package com.example.gomplay.domain.point.repository;

import com.example.gomplay.domain.point.entity.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PointLogRepository extends JpaRepository<PointLog, Long> {
    List<PointLog> findByUserProfile_IdOrderByCreatedAtDesc(Long userId);
    void deleteByGatheringId(Long gatheringId);
}