package com.example.gomplay.domain.team.dto;

import lombok.Getter;

@Getter
public class ReviewableParticipantResponse {
    private Long userId;
    private String name;
    private String profileImageUrl;
    private String department;
    private String studentNumber;
    private boolean reviewed;

    public static ReviewableParticipantResponse of(Long userId, String name, String profileImageUrl,
            String department, String studentNumber, boolean reviewed) {
        ReviewableParticipantResponse dto = new ReviewableParticipantResponse();
        dto.userId = userId;
        dto.name = name;
        dto.profileImageUrl = profileImageUrl;
        dto.department = department;
        dto.studentNumber = studentNumber;
        dto.reviewed = reviewed;
        return dto;
    }
}