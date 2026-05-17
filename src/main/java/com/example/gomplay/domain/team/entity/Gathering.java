package com.example.gomplay.domain.team.entity;

import com.example.gomplay.domain.user.entity.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "gathering")
public class Gathering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private UserProfile host;

    @Column(nullable = false)
    private String title;

    @Column(name = "sport_type", nullable = false)
    private String sportType;

    @Column(nullable = false)
    private String difficulty;

    @Column(nullable = false)
    private String venue;

    @Column(name = "venue_lat", nullable = false)
    private BigDecimal venueLat;

    @Column(name = "venue_lng", nullable = false)
    private BigDecimal venueLng;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants;

    @Column(name = "description")
    private String description;

    @Column(name = "scheduled_end_at")
    private LocalDateTime scheduledEndAt;

    @Column(name = "tags")
    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        OPEN, CLOSED, CANCELLED, COMPLETED
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.currentParticipants = 1;
        this.status = Status.OPEN;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String title, String sportType, String difficulty,
                   String venue, BigDecimal venueLat, BigDecimal venueLng,
                   LocalDateTime scheduledAt, LocalDateTime scheduledEndAt,
                   Integer maxParticipants, String description, String tags, String status) {
    if (title != null) this.title = title;
    if (sportType != null) this.sportType = sportType;
    if (difficulty != null) this.difficulty = difficulty;
    if (venue != null) this.venue = venue;
    if (venueLat != null) this.venueLat = venueLat;
    if (venueLng != null) this.venueLng = venueLng;
    if (scheduledAt != null) this.scheduledAt = scheduledAt;
    if (scheduledEndAt != null) this.scheduledEndAt = scheduledEndAt;
    if (maxParticipants != null) this.maxParticipants = maxParticipants;
    if (description != null) this.description = description;
    if (tags != null) this.tags = tags;
    if (status != null) this.status = Status.valueOf(status);
    }
}