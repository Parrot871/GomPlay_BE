package com.example.gomplay.domain.survey.repository;

import com.example.gomplay.domain.survey.entity.UserSurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserSurveyRepository extends JpaRepository<UserSurvey, Long> {
    Optional<UserSurvey> findByUserProfile_Id(Long userId);
}