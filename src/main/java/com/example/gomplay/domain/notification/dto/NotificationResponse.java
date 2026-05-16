package com.example.gomplay.domain.notification.dto;

import com.example.gomplay.domain.notification.entity.Notification;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class NotificationResponse {
    private Long id;
    private String type;
    private String title;
    private String body;
    private Long refId;
    private boolean isRead;
    private LocalDateTime createdAt;

    public NotificationResponse(Notification notification) {
        this.id = notification.getId();
        this.type = notification.getType().name();
        this.title = notification.getTitle();
        this.body = notification.getBody();
        this.refId = notification.getRefId();
        this.isRead = notification.isRead();
        this.createdAt = notification.getCreatedAt();
    }
}