package com.example.gomplay.domain.matching.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class MatchHistoryResponse {
    private Long id;
    private String type;
    private String status;
    private String role;
    private String partnerName;
    private String partnerProfileImageUrl;
    private String partnerDepartment;
    private String partnerStudentNumber;
    private String location;
    private String sportType;
    private LocalDateTime scheduledAt;
    private LocalDateTime matchedAt;
    private boolean reviewed;
}