package com.example.gomplay.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification",
        indexes = {
                @Index(name = "idx_email_verification_token", columnList = "token")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser authUser;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column
    private String name;

    @Column
    private String studentId;

    @Column
    private String department;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void updateToken(String token, LocalDateTime expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }
}