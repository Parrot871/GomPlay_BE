package com.example.gomplay.domain.user.entity;

import com.example.gomplay.domain.auth.entity.AuthUser;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_user_id", nullable = false, unique = true)
    private AuthUser authUser;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String department;

    @Column(name = "student_id")
    private String studentId;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "manner_temperature", nullable = false)
    private BigDecimal mannerTemperature;

    @Column(name = "no_show_count", nullable = false)
    private Integer noShowCount;

    @Column(name = "point_balance", nullable = false)
    private Integer pointBalance;

    @Column(name = "match_count", nullable = false)
    private Integer matchCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "exercise_types")
    private String exerciseTypes; // 쉼표로 구분 (예: "축구,농구")

    @Column(name = "difficulty")
    private String difficulty; // 난이도

    @Column(name = "bio")
    private String bio; // 한 줄 소개

    @Column(name = "timetable", columnDefinition = "TEXT")
    private String timetable; // 시간표

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.mannerTemperature == null)
            this.mannerTemperature = new BigDecimal("36.5");
        if (this.noShowCount == null) this.noShowCount = 0;
        if (this.pointBalance == null) this.pointBalance = 0;
        if (this.matchCount == null) this.matchCount = 0;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProfile(String profileImageUrl, String exerciseTypes, 
                           String difficulty, String bio, String timetable) {
    if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
    if (exerciseTypes != null) this.exerciseTypes = exerciseTypes;
    if (difficulty != null) this.difficulty = difficulty;
    if (bio != null) this.bio = bio;
    if (timetable != null) this.timetable = timetable;
    }
}