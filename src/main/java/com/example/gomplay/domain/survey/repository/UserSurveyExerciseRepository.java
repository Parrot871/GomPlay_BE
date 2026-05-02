package com.example.gomplay.domain.survey.repository;

import com.example.gomplay.domain.survey.entity.UserSurveyExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserSurveyExerciseRepository extends JpaRepository<UserSurveyExercise, Long> {
    List<UserSurveyExercise> findByUserProfile_Id(Long userId);
    void deleteByUserProfile_Id(Long userId);
}
