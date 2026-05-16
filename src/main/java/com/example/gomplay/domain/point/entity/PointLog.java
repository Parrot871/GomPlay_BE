package com.example.gomplay.domain.point.entity;

import com.example.gomplay.domain.team.entity.Gathering;
import com.example.gomplay.domain.user.entity.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile userProfile;

    @Column(name = "delta", nullable = false)
    private Integer delta;

    @Column(name = "balance_snapshot", nullable = false)
    private Integer balanceSnapshot;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "match_result_id")
    private Long matchResultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id")
    private Gathering gathering;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}