package com.konnectnet.core.notification.service;

import com.konnectnet.core.notification.dto.NotificationDTO;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface NotificationService {
    void sendNotification(NotificationDTO notificationDTO);
    Page<NotificationDTO> getNotificationsForUser(String userId, int page, int size);
    void markAsRead(String notificationId);
    void deleteNotification(String notificationId);
}