package com.example.gomplay.domain.groupchat.controller;

import com.example.gomplay.domain.groupchat.dto.GroupChatRoomDetailResponse;
import com.example.gomplay.domain.groupchat.dto.GroupChatRoomResponse;
import com.example.gomplay.domain.groupchat.dto.GroupChatScheduleRequest;
import com.example.gomplay.domain.groupchat.service.GroupChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;


import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-chat")
public class GroupChatController {

    private final GroupChatService groupChatService;

    // 내 채팅방 목록 조회
    @GetMapping("/rooms")
    public ResponseEntity<List<GroupChatRoomResponse>> getMyRooms(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(groupChatService.getMyRooms(userId));
    }

    // 채팅방 입장
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<GroupChatRoomDetailResponse> enterRoom(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId) {
        return ResponseEntity.ok(groupChatService.enterRoom(userId, roomId));
    }

    // 공지 전송 (방장만)
    @PostMapping("/rooms/{roomId}/notice")
    public ResponseEntity<Void> sendNotice(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId,
            @RequestBody Map<String, String> body) {
        groupChatService.sendNotice(userId, roomId, body.get("content"));
        return ResponseEntity.ok().build();
    }

    // 일정 전송 (방장만)
    @PostMapping("/rooms/{roomId}/schedule")
    public ResponseEntity<Void> sendSchedule(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId,
            @RequestBody GroupChatScheduleRequest request) {
        groupChatService.sendSchedule(userId, roomId, request);
        return ResponseEntity.ok().build();
    }

    // WebSocket 메시지 전송
    @MessageMapping("/group-chat/{roomId}")
    public void sendMessage(
            @DestinationVariable Long roomId,
            @Payload Map<String, String> payload,
            SimpMessageHeaderAccessor headerAccessor) {
        Long userProfileId = (Long) headerAccessor.getSessionAttributes().get("userProfileId");
        if (userProfileId == null) {
            throw new IllegalArgumentException("인증되지 않은 사용자입니다.");
        }
        groupChatService.sendMessageByProfileId(userProfileId, roomId, payload.get("content"));
    }
}