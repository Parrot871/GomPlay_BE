package com.example.gomplay.domain.survey.entity;

import com.example.gomplay.domain.user.entity.UserProfile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_survey_exercise")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSurveyExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile userProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false)
    private ExerciseType exerciseType;

    public enum ExerciseType {
        당구, 야구, 볼링, 자전거, 런닝, 축구, 풋살, 테니스, 등산, 농구, 배드민턴, 헬스
    }
}
