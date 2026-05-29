package com.example.gomplay.domain.groupchat.service;

import com.example.gomplay.domain.groupchat.dto.GroupChatMessageDto;
import com.example.gomplay.domain.groupchat.dto.GroupChatRoomDetailResponse;
import com.example.gomplay.domain.groupchat.dto.GroupChatRoomResponse;
import com.example.gomplay.domain.groupchat.dto.GroupChatScheduleRequest;
import com.example.gomplay.domain.groupchat.entity.GroupChatMessage;
import com.example.gomplay.domain.groupchat.entity.GroupChatRoom;
import com.example.gomplay.domain.groupchat.repository.GroupChatMessageRepository;
import com.example.gomplay.domain.groupchat.repository.GroupChatRoomRepository;
import com.example.gomplay.domain.team.entity.Gathering;
import com.example.gomplay.domain.team.entity.GatheringParticipant;
import com.example.gomplay.domain.team.repository.GatheringParticipantRepository;
import com.example.gomplay.domain.team.repository.GatheringRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import com.example.gomplay.domain.groupchat.dto.GroupChatParticipantDto;
import com.example.gomplay.domain.review.repository.ReviewRepository;
import com.example.gomplay.global.websocket.dto.WsMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final GroupChatRoomRepository groupChatRoomRepository;
    private final GroupChatMessageRepository groupChatMessageRepository;
    private final GatheringRepository gatheringRepository;
    private final GatheringParticipantRepository gatheringParticipantRepository;
    private final UserProfileRepository userProfileRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ReviewRepository reviewRepository;

    // 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<GroupChatRoomResponse> getMyRooms(Long userId) {
        UserProfile user = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 방장인 CLOSED/COMPLETED 모집글
        List<Long> gatheringIds = gatheringRepository.findByHost_Id(user.getId())
                .stream()
                .filter(g -> g.getStatus() == Gathering.Status.CLOSED || g.getStatus() == Gathering.Status.COMPLETED)
                .map(Gathering::getId)
                .collect(Collectors.toList());

        // ACCEPTED 참여자인 모집글
        gatheringParticipantRepository.findByUser_IdAndStatus(user.getId(), GatheringParticipant.Status.ACCEPTED)
                .stream()
                .filter(p -> p.getGathering().getStatus() == Gathering.Status.CLOSED || p.getGathering().getStatus() == Gathering.Status.COMPLETED)
                .map(p -> p.getGathering().getId())
                .forEach(gatheringIds::add);

        return gatheringIds.stream()
                .distinct()
                .map(gatheringId -> groupChatRoomRepository.findByGathering_Id(gatheringId))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(room -> {
                    GroupChatMessageDto lastMessage = groupChatMessageRepository
                            .findTopByRoom_IdOrderBySentAtDesc(room.getId())
                            .map(GroupChatMessageDto::of)
                            .orElse(null);
                    int participantCount = getParticipantCount(room.getGathering().getId());
                    return GroupChatRoomResponse.of(room, lastMessage, participantCount);
                })
                .collect(Collectors.toList());
    }

    //채팅방 입장
    @Transactional(readOnly = true)
    public GroupChatRoomDetailResponse enterRoom(Long userId, Long roomId) {
        GroupChatRoom room = groupChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        UserProfile user = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        validateParticipant(room, userId);

        Long gatheringId = room.getGathering().getId();
        boolean isHost = room.getGathering().getHost().getId().equals(user.getId());

        // reviewed 확인
        boolean reviewed = reviewRepository.existsByReviewer_IdAndGatheringId(user.getId(), gatheringId);

        // 참여자 목록 구성
        UserProfile host = room.getGathering().getHost();
        List<GroupChatParticipantDto> participants = new java.util.ArrayList<>();
        participants.add(GroupChatParticipantDto.of(host.getId(), host.getName(), host.getProfileImageUrl(), true));

        gatheringParticipantRepository.findByGathering_Id(gatheringId)
                .stream()
                .filter(p -> p.getStatus() == GatheringParticipant.Status.ACCEPTED)
                .forEach(p -> participants.add(
                        GroupChatParticipantDto.of(
                        p.getUser().getId(),
                        p.getUser().getName(),
                        p.getUser().getProfileImageUrl(),
                        false
                        )
                ));

        List<GroupChatMessageDto> messages = groupChatMessageRepository
                .findByRoom_IdOrderBySentAtAsc(roomId)
                .stream()
                .map(GroupChatMessageDto::of)
                .collect(Collectors.toList());

        int participantCount = getParticipantCount(gatheringId);

        return GroupChatRoomDetailResponse.of(room, messages, participantCount, isHost, reviewed, participants);
    }

    // 일반 메시지 전송
    @Transactional
    public void sendMessage(Long userId, Long roomId, String content) {
        GroupChatRoom room = groupChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        UserProfile sender = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        validateParticipant(room, userId);

        GroupChatMessage message = GroupChatMessage.createText(room, sender, content);
        groupChatMessageRepository.save(message);

        GroupChatMessageDto dto = GroupChatMessageDto.of(message);
        messagingTemplate.convertAndSend(
                "/topic/group-chat/" + roomId,
                WsMessage.builder().type("NEW_MESSAGE").data(dto).build()
        );
    }

    // 공지 전송 (방장만)
    @Transactional
    public void sendNotice(Long userId, Long roomId, String content) {
            GroupChatRoom room = groupChatRoomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

            UserProfile sender = userProfileRepository.findByAuthUser_Id(userId)
            .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

    if (!room.getGathering().getHost().getId().equals(sender.getId())) {
        throw new IllegalArgumentException("방장만 공지를 작성할 수 있습니다.");
    }

        GroupChatMessage message = GroupChatMessage.createNotice(room, sender, content);
        groupChatMessageRepository.save(message);

        GroupChatMessageDto dto = GroupChatMessageDto.of(message);
        messagingTemplate.convertAndSend(
                "/topic/group-chat/" + roomId,
                WsMessage.builder().type("NOTICE").data(dto).build()
        );
    }

    // 일정 전송 (방장만)
    @Transactional
    public void sendSchedule(Long userId, Long roomId, GroupChatScheduleRequest request) {
        GroupChatRoom room = groupChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        UserProfile sender = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        if (!room.getGathering().getHost().getId().equals(sender.getId())) {
                throw new IllegalArgumentException("방장만 일정을 작성할 수 있습니다.");
        }

        GroupChatMessage message = GroupChatMessage.createSchedule(
                room, sender, request.getContent(),
                request.getScheduledAt(), request.getVenue(), request.getSportType()
        );
        groupChatMessageRepository.save(message);

        GroupChatMessageDto dto = GroupChatMessageDto.of(message);
        messagingTemplate.convertAndSend(
                "/topic/group-chat/" + roomId,
                WsMessage.builder().type("SCHEDULE").data(dto).build()
        );
 }

    // 참여자 수 확인    
    private void validateParticipant(GroupChatRoom room, Long userId) {
        UserProfile user = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Long gatheringId = room.getGathering().getId();
        boolean isHost = room.getGathering().getHost().getId().equals(user.getId());
        boolean isAccepted = gatheringParticipantRepository
                .findByUser_IdAndStatus(user.getId(), GatheringParticipant.Status.ACCEPTED)
                .stream()
                .anyMatch(p -> p.getGathering().getId().equals(gatheringId));

        if (!isHost && !isAccepted) {
                throw new IllegalArgumentException("채팅방 참여자가 아닙니다.");
        }
    }

    // 참여자 수 계산
    private int getParticipantCount(Long gatheringId) {
        return (int) gatheringParticipantRepository
                .countByGathering_IdAndStatus(gatheringId, GatheringParticipant.Status.ACCEPTED) + 1;
    }

    // WebSocket용 메시지 전송 (userProfileId 기반)
    @Transactional
    public void sendMessageByProfileId(Long userProfileId, Long roomId, String content) {
        GroupChatRoom room = groupChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        UserProfile sender = userProfileRepository.findById(userProfileId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        validateParticipantByProfileId(room, userProfileId);

        GroupChatMessage message = GroupChatMessage.createText(room, sender, content);
        groupChatMessageRepository.save(message);

        GroupChatMessageDto dto = GroupChatMessageDto.of(message);
        messagingTemplate.convertAndSend(
                "/topic/group-chat/" + roomId,
                WsMessage.builder().type("NEW_MESSAGE").data(dto).build()
        );
 }

    private void validateParticipantByProfileId(GroupChatRoom room, Long userProfileId) {
        Long gatheringId = room.getGathering().getId();
        boolean isHost = room.getGathering().getHost().getId().equals(userProfileId);
        boolean isAccepted = gatheringParticipantRepository
                .findByUser_IdAndStatus(userProfileId, GatheringParticipant.Status.ACCEPTED)
                .stream()
                .anyMatch(p -> p.getGathering().getId().equals(gatheringId));

        if (!isHost && !isAccepted) {
                throw new IllegalArgumentException("채팅방 참여자가 아닙니다.");
        }
    }
}