package com.konnectnet.core.notification.kafka;

import com.konnectnet.core.friend.event.FriendRequestEvent;
import com.konnectnet.core.infrastructure.kafka.KafkaTopics;
import com.konnectnet.core.notification.dto.NotificationDTO;
import com.konnectnet.core.notification.enums.NotificationType;
import com.konnectnet.core.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;
    public static final String FRIEND_REQUEST_TOPIC = "friend-request-topic";

    @KafkaListener(topics = FRIEND_REQUEST_TOPIC, groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleFriendRequestEvent(FriendRequestEvent event) {
        log.info("Received FriendRequestEvent: {}", event);
        NotificationDTO notification = NotificationDTO.builder()
                .type(event.getType())
                .content(event.getContent())
                .senderName(event.getSenderName())
                .senderId(event.getSenderId())
                .recipientId(event.getRecipientId())
                .createdAt(Instant.now())
                .isRead(false)
                .build();

        notificationService.sendNotification(notification);
    }
}