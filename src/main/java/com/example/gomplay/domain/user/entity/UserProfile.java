package com.example.gomplay.domain.user.entity;

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

    @Column(name = "auth_user_id", nullable = false, unique = true)
    private Long authUserId;

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
}