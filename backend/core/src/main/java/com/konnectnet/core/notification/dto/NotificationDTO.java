package com.konnectnet.core.notification.dto;

import com.konnectnet.core.notification.enums.NotificationType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private String id;
    private NotificationType type;
    private String content;
    private String senderName;
    private String senderId;
    private String recipientId;
    private boolean isRead;
    private Instant createdAt;
}