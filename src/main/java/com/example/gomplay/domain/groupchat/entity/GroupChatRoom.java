package com.example.gomplay.domain.groupchat.entity;

import com.example.gomplay.domain.team.entity.Gathering;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false)
    private Gathering gathering;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static GroupChatRoom create(Gathering gathering) {
        GroupChatRoom room = new GroupChatRoom();
        room.gathering = gathering;
        return room;
    }
}