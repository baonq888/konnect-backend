package com.konnectnet.core.notification.websocket;

import com.konnectnet.core.notification.dto.NotificationDTO;
import com.konnectnet.core.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class NotificationWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @MessageMapping("/notifications")
    public void receiveNotification(NotificationDTO dto) {
        notificationService.sendNotification(dto);

        messagingTemplate.convertAndSendToUser(
                dto.getRecipientId(),
                "/queue/notifications",
                dto
        );
    }
}