package com.konnectnet.core.onlineuser;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

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
            // Broadcast that the user is online to all clients
            messagingTemplate.convertAndSend(
                    "/topic/online", new UserStatus(email, true)
            );
            // Send a list of online users to the newly connected user
            Set<String> onlineUsers = onlineUserService.getOnlineUsers();
            messagingTemplate.convertAndSendToUser(
                    email, "/queue/online-users", onlineUsers
            );
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
