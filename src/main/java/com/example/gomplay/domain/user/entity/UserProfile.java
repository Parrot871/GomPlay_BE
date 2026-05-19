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

    @Column(name = "is_matching", nullable = false)
    private boolean isMatching = false;

    @Column(name = "college")
    private String college;



    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.profileImageUrl == null) {
            this.profileImageUrl = "https://gomplay-storage.s3.ap-northeast-2.amazonaws.com/default_profile.png";
        }
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

    public void updateProfile(String profileImageUrl) {
    if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
    }

    public void updateMatchingStatus(boolean isMatching) {
        this.isMatching = isMatching;
    }

    public void addPoint(int point) {
    this.pointBalance += point;
    }

    public void incrementMatchCount() {
    this.matchCount += 1;
    }

    public void incrementNoShowCount() {
    this.noShowCount += 1;  
    }

    public void updateMannerTemperature(BigDecimal temperature) {
    this.mannerTemperature = temperature;
    }

    @Column(name = "matching_restricted_until")
    private LocalDateTime matchingRestrictedUntil;

    public void restrictMatching(LocalDateTime until) {
        this.matchingRestrictedUntil = until;
    }
}