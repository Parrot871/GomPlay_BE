
package com.example.gomplay.domain.chat.service;

import com.example.gomplay.domain.chat.dto.ChatMessageDto;
import com.example.gomplay.domain.chat.dto.ChatRoomResponse;
import com.example.gomplay.domain.chat.dto.ChatRoomDetailResponse;
import com.example.gomplay.domain.chat.entity.ChatMessage;
import com.example.gomplay.domain.chat.entity.ChatRoom;
import com.example.gomplay.domain.chat.repository.ChatMessageRepository;
import com.example.gomplay.domain.chat.repository.ChatRoomRepository;
import com.example.gomplay.domain.matching.entity.MatchResult;
import com.example.gomplay.domain.matching.repository.MatchResultRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import com.example.gomplay.global.websocket.dto.WsMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MatchResultRepository matchResultRepository;
    private final UserProfileRepository userProfileRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 채팅방 입장 (메시지 내역 + 읽음 처리)
    @Transactional
    public ChatRoomDetailResponse enterRoom(Long userId, Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        validateParticipant(room, userId);

        long unreadCount = chatMessageRepository.countByRoom_IdAndIsReadFalseAndSender_IdNot(roomId, userId);

        chatMessageRepository.markAsRead(roomId, userId);

        List<ChatMessageDto> messages = chatMessageRepository
                .findByRoom_IdOrderBySentAtAsc(roomId)
                .stream()
                .map(ChatMessageDto::of)
                .collect(Collectors.toList());

        return ChatRoomDetailResponse.of(room, userId, messages, unreadCount);  // 여기
    }

    // 메시지 전송
    @Transactional
    public void sendMessage(Long userId, Long roomId, String content) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // 참여자 확인
        validateParticipant(room, userId);

        // COMPLETED 상태면 전송 불가
        if (room.getMatchResult().getStatus() == MatchResult.MatchResultStatus.COMPLETED) {
            throw new IllegalArgumentException("완료된 매칭의 채팅방입니다.");
        }

        UserProfile sender = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        ChatMessage message = ChatMessage.create(room, sender, content);
        chatMessageRepository.save(message);

        ChatMessageDto dto = ChatMessageDto.of(message);

        // 상대방에게 웹소켓 푸시
        Long opponentId = room.getUserA().getId().equals(userId)
                ? room.getUserB().getId()
                : room.getUserA().getId();

        messagingTemplate.convertAndSendToUser(
                opponentId.toString(),
                "/queue/chat",
                WsMessage.builder().type("NEW_MESSAGE").data(dto).build()
        );
    }

    // 운동 완료 처리
    @Transactional
    public void completeMatch(Long userId, Long matchResultId) {
        MatchResult matchResult = matchResultRepository.findById(matchResultId)
                .orElseThrow(() -> new IllegalArgumentException("매칭 결과를 찾을 수 없습니다."));

        if (matchResult.getStatus() != MatchResult.MatchResultStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("진행 중인 매칭이 아닙니다.");
        }

        boolean bothCompleted = matchResult.markCompleted(userId);

        // 상대방 ID 구하기
        Long opponentId = matchResult.getUserA().getId().equals(userId)
                ? matchResult.getUserB().getId()
                : matchResult.getUserA().getId();

        if (bothCompleted) {
            // 양쪽에 MATCH_COMPLETED 푸시
            messagingTemplate.convertAndSendToUser(
                    matchResult.getUserA().getId().toString(),
                    "/queue/chat",
                    WsMessage.builder().type("MATCH_COMPLETED").data(matchResultId).build()
            );
            messagingTemplate.convertAndSendToUser(
                    matchResult.getUserB().getId().toString(),
                    "/queue/chat",
                    WsMessage.builder().type("MATCH_COMPLETED").data(matchResultId).build()
            );
        } else {
            // 상대방에게만 "파트너가 완료 눌렀음" 푸시
            messagingTemplate.convertAndSendToUser(
                    opponentId.toString(),
                    "/queue/chat",
                    WsMessage.builder().type("PARTNER_COMPLETED").data(matchResultId).build()
            );
        }
    }

    // 12시간 지난 IN_PROGRESS 자동 완료 (1분마다 체크)
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void autoComplete() {
        log.info("autoComplete 스케줄러 실행");
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(2); // minusHours(2) → minusMinutes(2) 2분에서 24시간으로 수정해야함
        List<ChatRoom> rooms = chatRoomRepository
                .findByCreatedAtBeforeAndMatchResult_Status(
                        threshold, MatchResult.MatchResultStatus.IN_PROGRESS);
        log.info("처리 대상 채팅방 수: {}", rooms.size());
        rooms.forEach(room -> {
            MatchResult matchResult = room.getMatchResult();
            matchResult.complete();
            messagingTemplate.convertAndSendToUser(
                    matchResult.getUserA().getId().toString(),
                    "/queue/chat",
                    WsMessage.builder().type("MATCH_COMPLETED").data(matchResult.getId()).build()
            );
            messagingTemplate.convertAndSendToUser(
                    matchResult.getUserB().getId().toString(),
                    "/queue/chat",
                    WsMessage.builder().type("MATCH_COMPLETED").data(matchResult.getId()).build()
            );
        });
    }

    private void validateParticipant(ChatRoom room, Long userId) {
        if (!room.getUserA().getId().equals(userId) && !room.getUserB().getId().equals(userId)) {
            throw new IllegalArgumentException("채팅방 참여자가 아닙니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> myRooms(Long userId) {
        return chatRoomRepository.findByUserA_IdOrUserB_Id(userId, userId)
                .stream()
                .map(room -> {
                    // 마지막 메시지 1개만 조회
                    ChatMessageDto lastMessage = chatMessageRepository
                            .findTopByRoom_IdOrderBySentAtDesc(room.getId())
                            .map(ChatMessageDto::of)
                            .orElse(null);
                    long unreadCount = chatMessageRepository
                            .countByRoom_IdAndIsReadFalseAndSender_IdNot(room.getId(), userId);
                    return ChatRoomResponse.of(room, userId, lastMessage, unreadCount);
                })
                .collect(Collectors.toList());
    }
}

