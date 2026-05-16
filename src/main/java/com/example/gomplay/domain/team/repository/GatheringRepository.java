package com.example.gomplay.domain.team.repository;

import com.example.gomplay.domain.team.entity.Gathering;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface GatheringRepository extends JpaRepository<Gathering, Long> {
    Page<Gathering> findBySportType(String sportType, Pageable pageable);
    Page<Gathering> findByDifficulty(String difficulty, Pageable pageable);
    Page<Gathering> findBySportTypeAndDifficulty(String sportType, String difficulty, Pageable pageable);
    List<Gathering> findByStatusAndHostIdNot(Gathering.Status status, Long hostId);
}

