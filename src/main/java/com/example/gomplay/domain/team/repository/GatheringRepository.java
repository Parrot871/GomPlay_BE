package com.example.gomplay.domain.team.repository;

import com.example.gomplay.domain.team.entity.Gathering;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GatheringRepository extends JpaRepository<Gathering, Long> {
    Page<Gathering> findBySportType(String sportType, Pageable pageable);
    Page<Gathering> findByDifficulty(String difficulty, Pageable pageable);
    Page<Gathering> findBySportTypeAndDifficulty(String sportType, String difficulty, Pageable pageable);
    List<Gathering> findByStatusAndHostIdNot(Gathering.Status status, Long hostId);

    List<Gathering> findByHost_IdAndStatus(Long hostId, Gathering.Status status);

    @Query("SELECT g FROM Gathering g JOIN GatheringParticipant gp ON g.id = gp.gathering.id WHERE gp.user.id = :userId AND g.status = :status")
    List<Gathering> findByParticipantIdAndStatus(@Param("userId") Long userId, @Param("status") Gathering.Status status);

    List<Gathering> findByStatusInAndScheduledEndAtBefore(List<Gathering.Status> statuses, LocalDateTime dateTime);
    Page<Gathering> findByStatus(Gathering.Status status, Pageable pageable);
    Page<Gathering> findBySportTypeAndStatus(String sportType, Gathering.Status status, Pageable pageable);
    Page<Gathering> findByDifficultyAndStatus(String difficulty, Gathering.Status status, Pageable pageable);
    Page<Gathering> findBySportTypeAndDifficultyAndStatus(String sportType, String difficulty, Gathering.Status status, Pageable pageable);
    List<Gathering> findByBoostedTrueAndBoostExpiredAtBefore(LocalDateTime dateTime);
    List<Gathering> findByHost_Id(Long hostId);


}

