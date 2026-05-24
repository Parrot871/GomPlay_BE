package com.example.gomplay.domain.team.dto;

import com.example.gomplay.domain.team.entity.GatheringParticipant;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class GatheringParticipantResponse {
    private Long id;
    private Long gatheringId;
    private Long userId;
    private String userName;
    private String userProfileImageUrl;
    private String status;
    private LocalDateTime updatedAt;
    private BigDecimal mannerTemperature;
    private String department;
    private String studentId;

    public GatheringParticipantResponse(GatheringParticipant participant) {
        this.id = participant.getId();
        this.gatheringId = participant.getGathering().getId();
        this.userId = participant.getUser().getId();
        this.userName = participant.getUser().getName();
        this.userProfileImageUrl = participant.getUser().getProfileImageUrl();
        this.status = participant.getStatus().name();
        this.updatedAt = participant.getUpdatedAt();
        this.mannerTemperature = participant.getUser().getMannerTemperature();
        this.department = participant.getUser().getDepartment();
        this.studentId = participant.getUser().getStudentId();
    }
}