package com.example.gomplay.domain.matching.entity;

import com.example.gomplay.domain.user.entity.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MatchRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private UserProfile requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opponent_id", nullable = false)
    private UserProfile opponent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchRequestStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum MatchRequestStatus {
        PENDING, ACCEPTED, REJECTED, TIMEOUT
    }

    public static MatchRequest create(UserProfile requester, UserProfile opponent) {
        return MatchRequest.builder()
                .requester(requester)
                .opponent(opponent)
                .status(MatchRequestStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusSeconds(30))
                .build();
    }

    public void accept() {
        this.status = MatchRequestStatus.ACCEPTED;
    }

    public void reject() {
        this.status = MatchRequestStatus.REJECTED;
    }

    public void timeout() {
        this.status = MatchRequestStatus.TIMEOUT;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}