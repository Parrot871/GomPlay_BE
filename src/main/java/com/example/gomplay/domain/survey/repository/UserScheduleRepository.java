package com.example.gomplay.domain.survey.repository;

import com.example.gomplay.domain.survey.entity.UserSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserScheduleRepository extends JpaRepository<UserSchedule, Long> {
    List<UserSchedule> findByUserProfile_Id(Long userId);
}
