package com.example.gomplay.domain.matching.dto;

import com.example.gomplay.domain.team.entity.Gathering;
import com.example.gomplay.domain.matching.entity.MatchResult;
import com.example.gomplay.domain.chat.entity.ChatRoom;
import com.example.gomplay.domain.user.entity.UserProfile;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
public class ActiveMatchResponse {
    private Long id;
    private String type;
    private String status;
    private String role;
    private String partnerName;
    private String partnerProfileImageUrl;
    private String partnerDepartment;
    private String partnerStudentNumber;
    private String location;
    private String scheduledTime;
    private String scheduledAt;
    private String scheduledEndAt;
    private String difficulty;
    private String sportType;
    private Long chatRoomId;
    private String matchedAt;
    private long pendingCount;
    private boolean reviewed;
    private boolean canComplete;
    private String canCompleteReason; // NOT_STARTED, ALREADY_REQUESTED, AVAILABLE

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
            .withZone(ZoneId.of("Asia/Seoul"));

    private static String toZonedString(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.atZone(ZoneId.of("Asia/Seoul")).format(FORMATTER);
    }

    // GATHERING용 (HOST / ACCEPTED GUEST 공통)
    public static ActiveMatchResponse ofGathering(
            Gathering gathering, UserProfile me, UserProfile partner,
            long pendingCount, boolean reviewed, boolean iCompleted) {

            boolean isHost = gathering.getHost().getId().equals(me.getId());

            ActiveMatchResponse res = new ActiveMatchResponse();
            res.id = gathering.getId();
            res.type = "GATHERING";
            res.status = toGatheringStatus(gathering);
            res.role = isHost ? "HOST" : "GUEST";

            UserProfile displayPartner = isHost ? partner : gathering.getHost();
            res.partnerName = displayPartner != null ? displayPartner.getName() : null;
            res.partnerProfileImageUrl = displayPartner != null ? displayPartner.getProfileImageUrl() : null;
            res.partnerDepartment = displayPartner != null ? displayPartner.getDepartment() : null;
            res.partnerStudentNumber = displayPartner != null ? displayPartner.getStudentId() : null;

            res.location = gathering.getVenue();
            res.scheduledAt = toZonedString(gathering.getScheduledAt());
            res.scheduledEndAt = toZonedString(gathering.getScheduledEndAt());
            res.scheduledTime = formatScheduledTime(gathering.getScheduledAt(), gathering.getScheduledEndAt());
            res.difficulty = gathering.getDifficulty();
            res.sportType = gathering.getSportType();
            res.chatRoomId = null;
            res.matchedAt = null;
            res.pendingCount = pendingCount;
            res.reviewed = reviewed;

            // canCompleteReason 판단
            LocalDateTime now = LocalDateTime.now();
            boolean scheduledPassed = gathering.getScheduledAt() != null
                    && gathering.getScheduledAt().isBefore(now);

            if (!scheduledPassed) {
                res.canComplete = false;
                res.canCompleteReason = "NOT_STARTED";
            } else if (iCompleted) {
                res.canComplete = false;
                res.canCompleteReason = "ALREADY_REQUESTED";
            } else {
                res.canComplete = true;
                res.canCompleteReason = "AVAILABLE";
            }

            return res;
    }

    // GATHERING PENDING 신청자용
    public static ActiveMatchResponse ofGatheringPending(
            Gathering gathering, UserProfile me, UserProfile host, boolean reviewed) {

        ActiveMatchResponse res = new ActiveMatchResponse();
        res.id = gathering.getId();
        res.type = "GATHERING";
        res.status = "PENDING";
        res.role = "GUEST";
        res.partnerName = host.getName();
        res.partnerProfileImageUrl = host.getProfileImageUrl();
        res.partnerDepartment = host.getDepartment();
        res.partnerStudentNumber = host.getStudentId();
        res.location = gathering.getVenue();
        res.scheduledAt = toZonedString(gathering.getScheduledAt());
        res.scheduledEndAt = toZonedString(gathering.getScheduledEndAt());
        res.scheduledTime = formatScheduledTime(gathering.getScheduledAt(), gathering.getScheduledEndAt());
        res.difficulty = gathering.getDifficulty();
        res.sportType = gathering.getSportType();
        res.chatRoomId = null;
        res.matchedAt = null;
        res.pendingCount = 0L;
        res.reviewed = reviewed;
        res.canComplete = false;
        return res;
    }

    // PARTNER용
    public static ActiveMatchResponse ofPartner(
            MatchResult matchResult, UserProfile me, UserProfile partner,
            ChatRoom chatRoom, boolean reviewed) {

        ActiveMatchResponse res = new ActiveMatchResponse();
        res.id = matchResult.getId();
        res.type = "PARTNER";
        res.status = matchResult.getStatus().name();
        res.role = null;
        res.partnerName = partner.getName();
        res.partnerProfileImageUrl = partner.getProfileImageUrl();
        res.partnerDepartment = partner.getDepartment();
        res.partnerStudentNumber = partner.getStudentId();
        res.location = null;
        res.scheduledTime = null;
        res.scheduledAt = null;
        res.scheduledEndAt = null;
        res.difficulty = null;
        res.sportType = null;
        res.chatRoomId = chatRoom != null ? chatRoom.getId() : null;
        res.matchedAt = toZonedString(matchResult.getCreatedAt());
        res.pendingCount = 0;
        res.reviewed = reviewed;
        res.canComplete = matchResult.getStatus() == MatchResult.MatchResultStatus.IN_PROGRESS
                && matchResult.getCreatedAt().plusSeconds(10).isBefore(LocalDateTime.now());
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