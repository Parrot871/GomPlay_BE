package com.example.gomplay.domain.notification.service;

import com.example.gomplay.domain.notification.dto.NotificationResponse;
import com.example.gomplay.domain.notification.entity.Notification;
import com.example.gomplay.domain.notification.repository.NotificationRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserProfileRepository userProfileRepository;

    // 전체 알림 조회
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId, String tab) {
        UserProfile user = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        List<Notification> notifications;

       if ("partner".equals(tab)) {
            notifications = notificationRepository
            .findByUserProfile_IdAndTypeInOrderByCreatedAtDesc(
                    user.getId(),
                    List.of(
                            Notification.NotificationType.match_request,
                            Notification.NotificationType.match_accepted,
                            Notification.NotificationType.match_rejected
                    )
            );
            } else if ("general".equals(tab)) {
                 notifications = notificationRepository
                    .findByUserProfile_IdAndTypeInOrderByCreatedAtDesc(
                    user.getId(),
                    List.of(
                            Notification.NotificationType.gathering,
                            Notification.NotificationType.gathering_request,
                            Notification.NotificationType.review_available,
                            Notification.NotificationType.match_end_confirm,
                            Notification.NotificationType.match_auto_ended
                    )
            );
} else {
    // 전체 탭 - 모든 알림
    notifications = notificationRepository
            .findByUserProfile_IdOrderByCreatedAtDesc(user.getId());
}


        return notifications.stream()
                .map(NotificationResponse::new)
                .collect(Collectors.toList());
    }

    // 전체 읽음 처리
    @Transactional
    public void markAllAsRead(Long userId) {
        UserProfile user = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        List<Notification> unreadNotifications = notificationRepository
                .findByUserProfile_IdAndIsReadFalse(user.getId());

        unreadNotifications.forEach(Notification::markAsRead);
    }
}