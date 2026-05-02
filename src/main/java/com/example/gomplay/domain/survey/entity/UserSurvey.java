package com.example.gomplay.domain.survey.entity;

import com.example.gomplay.domain.user.entity.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_survey")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSurvey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserProfile userProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "partner_style", nullable = false)
    private PartnerStyle partnerStyle;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_intensity", nullable = false)
    private ExerciseIntensity exerciseIntensity;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_reason", nullable = false)
    private ExerciseReason exerciseReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    public enum PartnerStyle {
        각자, 같이
    }

    public enum ExerciseIntensity {
        가볍게, 적당히, 제대로, 한계까지
    }

    public enum ExerciseReason {
        스트레스, 친해지려고, 경쟁, 체력
    }

}
