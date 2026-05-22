package com.example.gomplay.domain.matching.dto;

import com.example.gomplay.domain.team.entity.Gathering;
import com.example.gomplay.domain.team.entity.GatheringParticipant;
import com.example.gomplay.domain.matching.entity.MatchResult;
import com.example.gomplay.domain.chat.entity.ChatRoom;
import com.example.gomplay.domain.user.entity.UserProfile;
import lombok.Getter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class ActiveMatchResponse {
    private Long id;
    private String type;        // GATHERING, PARTNER
    private String status;
    private String role;        // HOST, GUEST (GATHERING만)
    private String partnerName;
    private String partnerProfileImageUrl;
    private String partnerDepartment;
    private String partnerStudentNumber;
    private String location;
    private String scheduledTime;
    private LocalDateTime scheduledAt;
    private LocalDateTime scheduledEndAt;
    private String difficulty;
    private String sportType;
    private Long chatRoomId;
    private LocalDateTime matchedAt;
    private long pendingCount;
    private boolean reviewed;
    private boolean canComplete;

    // GATHERING용
    public static ActiveMatchResponse ofGathering(
            Gathering gathering, UserProfile me, UserProfile partner,
            long pendingCount, boolean reviewed) {

        ActiveMatchResponse res = new ActiveMatchResponse();
        res.id = gathering.getId();
        res.type = "GATHERING";
        res.status = toGatheringStatus(gathering);
        res.role = gathering.getHost().getId().equals(me.getId()) ? "HOST" : "GUEST";
        res.partnerName = partner != null ? partner.getName() : null;
        res.partnerProfileImageUrl = partner != null ? partner.getProfileImageUrl() : null;
        res.partnerDepartment = partner != null ? partner.getDepartment() : null;
        res.partnerStudentNumber = partner != null ? partner.getStudentId() : null;
        res.location = gathering.getVenue();
        res.scheduledAt= gathering.getScheduledAt();
        res.scheduledEndAt = gathering.getScheduledEndAt();
        res.difficulty = gathering.getDifficulty();
        res.sportType = gathering.getSportType();
        res.chatRoomId = null;  // gathering은 채팅방 없음
        res.matchedAt = null;
        res.pendingCount = pendingCount;
        res.reviewed = reviewed;
        res.canComplete = gathering.getScheduledEndAt() != null
                && gathering.getScheduledEndAt().isBefore(LocalDateTime.now())
                && gathering.getStatus() == Gathering.Status.OPEN;
        return res;
    }

    // PARTNER용
    public static ActiveMatchResponse ofPartner(
            MatchResult matchResult, UserProfile me, UserProfile partner,
            ChatRoom chatRoom, boolean reviewed) {

        ActiveMatchResponse res = new ActiveMatchResponse();
        res.id = matchResult.getId();
        res.type = "PARTNER";
        res.status = matchResult.getStatus().name();  // IN_PROGRESS, COMPLETED
        res.role = null;
        res.partnerName = partner.getName();
        res.partnerProfileImageUrl = partner.getProfileImageUrl();
        res.partnerDepartment = partner.getDepartment();
        res.partnerStudentNumber = partner.getStudentId();
        res.location = null;
        res.scheduledTime = null;
        res.scheduledEndAt = null;
        res.difficulty = null;
        res.sportType = null;
        res.chatRoomId = chatRoom != null ? chatRoom.getId() : null;
        res.matchedAt = matchResult.getCreatedAt();
        res.pendingCount = 0;
        res.reviewed = reviewed;
        res.canComplete = matchResult.getStatus() == MatchResult.MatchResultStatus.IN_PROGRESS
                && matchResult.getCreatedAt().plusMinutes(1).isBefore(LocalDateTime.now());
        return res;
    }

    private static String toGatheringStatus(Gathering gathering) {
        return switch (gathering.getStatus()) {
            case OPEN -> "PENDING";
            case COMPLETED -> "COMPLETED";
            case CANCELLED -> "CANCELLED";
            default -> gathering.getStatus().name();
        };
    }

    private static String formatScheduledTime(LocalDateTime start, LocalDateTime end) {
        if (start == null) return null;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        if (end == null) return start.format(fmt);
        return start.format(fmt) + " ~ " + end.format(fmt);
    }
}