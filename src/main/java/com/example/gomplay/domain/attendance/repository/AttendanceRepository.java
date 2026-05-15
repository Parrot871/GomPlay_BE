package com.example.gomplay.domain.attendance.repository;

import com.example.gomplay.domain.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    boolean existsByUserProfile_IdAndDate(Long userId, LocalDate date);
    List<Attendance> findByUserProfile_IdAndDateBetween(Long userId, LocalDate start, LocalDate end);
    int countByUserProfile_Id(Long userId);
}