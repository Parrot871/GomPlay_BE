
package com.example.gomplay.domain.matching.entity;

import com.example.gomplay.domain.user.entity.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_request_id", nullable = false)
    private MatchRequest matchRequest;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_a_id", nullable = false)
    private UserProfile userA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_b_id", nullable = false)
    private UserProfile userB;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchResultStatus status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "user_a_completed", nullable = false)
    private boolean userACompleted = false;

    @Column(name = "user_b_completed", nullable = false)
    private boolean userBCompleted = false;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum MatchResultStatus{
        IN_PROGRESS, COMPLETED, CANCELLED
    }

    // 내가 완료 버튼 눌렀을 때
    public boolean markCompleted(Long userId) {
        if (this.userA.getId().equals(userId)) {
            this.userACompleted = true;
        } else {
            this.userBCompleted = true;
        }
        // 둘 다 눌렀으면 true 반환
        if (this.userACompleted && this.userBCompleted) {
            complete();
            return true;
        }
        return false;
    }

    public static MatchResult create(MatchRequest matchRequest, UserProfile userA, UserProfile userB) {
        return MatchResult.builder()
                .matchRequest(matchRequest)
                .userA(userA)
                .userB(userB)
                .status(MatchResultStatus.IN_PROGRESS)
                .build();
    }

    public void complete() {
        this.status = MatchResultStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = MatchResultStatus.CANCELLED;
    }
}

