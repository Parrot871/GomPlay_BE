
package com.example.gomplay.domain.chat;

import com.example.gomplay.domain.chat.dto.ChatRoomResponse;
import com.example.gomplay.domain.chat.dto.SendMessageRequest;
import com.example.gomplay.domain.chat.dto.ChatRoomDetailResponse;
import com.example.gomplay.domain.chat.service.ChatService;
import com.example.gomplay.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    // 채팅방 입장 (메시지 내역 + 읽음 처리)
    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomDetailResponse>> enterRoom(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId) {
        return ResponseEntity.ok(ApiResponse.success(
                "채팅방 입장 성공",
                chatService.enterRoom(userId, roomId)
        ));
    }

    // 메시지 전송
    @MessageMapping("/chat/room/{roomId}/message")
    public void sendMessage(@DestinationVariable Long roomId,
                            @Payload SendMessageRequest request,
                            Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        chatService.sendMessage(userId, roomId, request.getContent());
    }

    // 운동 완료 처리
    @PatchMapping("/result/{matchResultId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeMatch(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long matchResultId) {
        chatService.completeMatch(userId, matchResultId);
        return ResponseEntity.ok(ApiResponse.success("운동이 완료되었습니다.", null));
    }

    // 내 채팅방 목록
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<?>> myRooms(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "채팅방 목록 조회 성공",
                chatService.myRooms(userId)
        ));
    }
}
