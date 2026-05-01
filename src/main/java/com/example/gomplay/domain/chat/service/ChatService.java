package com.example.gomplay.domain.chat.service;

import com.example.gomplay.domain.chat.entity.ChatMessage;
import com.example.gomplay.domain.chat.entity.ChatRoom;
import com.example.gomplay.domain.chat.repository.ChatMessageRepository;
import com.example.gomplay.domain.chat.repository.ChatRoomRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserProfileRepository userProfileRepository;

    // 채팅방 생성
    @Transactional
    public ChatRoom createChatRoom(Long userAId, Long userBId) {
        return chatRoomRepository
                .findByUserAIdAndUserBId(userAId, userBId)
                .orElseGet(() -> {
                    UserProfile userA = userProfileRepository.findById(userAId)
                            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
                    UserProfile userB = userProfileRepository.findById(userBId)
                            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
                    ChatRoom chatRoom = ChatRoom.builder()
                            .userA(userA)
                            .userB(userB)
                            .build();
                    return chatRoomRepository.save(chatRoom);
                });
    }

    // 메시지 저장
    @Transactional
    public ChatMessage saveMessage(Long roomId, Long senderId, String content) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
        UserProfile sender = userProfileRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        ChatMessage message = new ChatMessage(chatRoom, sender, content);
        return chatMessageRepository.save(message);
    }

    // 채팅방 메시지 목록 조회
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(Long roomId) {
        return chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(roomId);
    }

    // 내 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<ChatRoom> getChatRooms(Long userId) {
        return chatRoomRepository.findByUserId(userId);
    }
}