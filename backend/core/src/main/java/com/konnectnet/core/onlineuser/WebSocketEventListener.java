package com.konnectnet.core.onlineuser;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final SimpMessageSendingOperations messagingTemplate;
    private final OnlineUserService onlineUserService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        if (event.getUser() != null) {
            String email = event.getUser().getName();
            onlineUserService.addUser(email);
        }
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        Principal principal = event.getUser();

        // Check if the subscription is for the online users queue and user is authenticated
        if (principal != null && "/user/queue/online-users".equals(destination)) {
            String email = principal.getName();

            Set<String> onlineUsers = onlineUserService.getOnlineUsers();

            if (onlineUsers != null) {
                String userQueueDestination = "/queue/online-users";
                messagingTemplate.convertAndSendToUser(email, userQueueDestination, onlineUsers);
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        if (event.getUser() != null) {
            String email = event.getUser().getName();
            onlineUserService.removeUser(email);
            // Broadcast that the user is offline to all client
            messagingTemplate.convertAndSend(
                    "/topic/online", new UserStatus(email, false)
            );
        }
    }
}
