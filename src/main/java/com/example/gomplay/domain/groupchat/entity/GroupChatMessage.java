package com.example.gomplay.domain.groupchat.entity;

import com.example.gomplay.domain.user.entity.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private GroupChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserProfile sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "venue")
    private String venue;

    @Column(name = "sport_type")
    private String sportType;

    @CreationTimestamp
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    public enum MessageType {
        TEXT, NOTICE, SCHEDULE
    }

    public static GroupChatMessage createText(GroupChatRoom room, UserProfile sender, String content) {
        GroupChatMessage msg = new GroupChatMessage();
        msg.room = room;
        msg.sender = sender;
        msg.content = content;
        msg.messageType = MessageType.TEXT;
        return msg;
    }

    public static GroupChatMessage createNotice(GroupChatRoom room, UserProfile sender, String content) {
        GroupChatMessage msg = new GroupChatMessage();
        msg.room = room;
        msg.sender = sender;
        msg.content = content;
        msg.messageType = MessageType.NOTICE;
        return msg;
    }

    public static GroupChatMessage createSchedule(GroupChatRoom room, UserProfile sender,
            String content, LocalDateTime scheduledAt, String venue, String sportType) {
        GroupChatMessage msg = new GroupChatMessage();
        msg.room = room;
        msg.sender = sender;
        msg.content = content;
        msg.messageType = MessageType.SCHEDULE;
        msg.scheduledAt = scheduledAt;
        msg.venue = venue;
        msg.sportType = sportType;
        return msg;
    }
}