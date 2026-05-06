package com.example.gomplay.domain.matching.entity;

import com.example.gomplay.domain.user.entity.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="quick_match_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuickMatchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile userProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum MatchStatus {
        WAITING, MATCHED, CANCELLED
    }

    public static QuickMatchLog createWaiting(UserProfile userProfile) {
        QuickMatchLog log = new QuickMatchLog();
        log.userProfile = userProfile;
        log.status = MatchStatus.WAITING;
        return log;
    }

    public void cancel() {
        this.status = MatchStatus.CANCELLED;
    }
}
