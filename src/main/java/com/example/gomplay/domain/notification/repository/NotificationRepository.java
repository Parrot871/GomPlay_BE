package com.example.gomplay.domain.notification.repository;

import com.example.gomplay.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserProfile_IdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserProfile_IdAndTypeInOrderByCreatedAtDesc(Long userId, List<Notification.NotificationType> types);
    List<Notification> findByUserProfile_IdAndIsReadFalse(Long userId);
}