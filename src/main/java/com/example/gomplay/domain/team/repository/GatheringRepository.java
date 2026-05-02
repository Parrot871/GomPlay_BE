package com.example.gomplay.domain.team.repository;

import com.example.gomplay.domain.team.entity.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringRepository extends JpaRepository<Gathering, Long> {
}