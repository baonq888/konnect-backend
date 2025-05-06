package com.konnectnet.core.notification.service.impl;

import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.auth.repository.UserRepository;
import com.konnectnet.core.notification.dto.NotificationDTO;
import com.konnectnet.core.notification.entity.Notification;
import com.konnectnet.core.notification.enums.NotificationType;
import com.konnectnet.core.notification.mapper.NotificationMapper;
import com.konnectnet.core.notification.repository.NotificationRepository;
import com.konnectnet.core.notification.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public void sendNotification(NotificationDTO dto) {
        AppUser sender = userRepository
                .findById(UUID.fromString(dto.getSenderId()))
                .orElseThrow(() -> new EntityNotFoundException("Sender not found")
        );
        AppUser recipient = userRepository
                .findById(UUID.fromString(dto.getRecipientId()))
                .orElseThrow(() -> new EntityNotFoundException("Recipient not found"));

        Notification notification = Notification.builder()
                .sender(sender)
                .recipient(recipient)
                .content(dto.getContent())
                .type(NotificationType.valueOf(String.valueOf(dto.getType())))
                .read(false)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    public Page<NotificationDTO> getNotificationsForUser(String userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        AppUser sender = userRepository
                .findById(UUID.fromString(userId))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Page<Notification> notificationsPage =
                notificationRepository.findByRecipientIdOrderByCreatedAtDesc(
                        UUID.fromString(userId), pageRequest);

        return notificationsPage.map(notificationMapper::toNotificationDTO);
    }

    @Override
    public void markAsRead(String notificationId) {
        Notification notification =
                notificationRepository
                        .findById(UUID.fromString(notificationId))
                        .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void deleteNotification(String notificationId) {
        notificationRepository
                .findById(UUID.fromString(notificationId))
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        notificationRepository.deleteById(UUID.fromString(notificationId));
    }
}