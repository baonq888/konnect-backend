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
            System.out.println("--- WebSocket Session Connected for user: " + email + " ---");

            onlineUserService.addUser(email);
            System.out.println("--- Added user '" + email + "' to online list ---");

            // Broadcast user status (useful for other clients, not directly tested here)
            // messagingTemplate.convertAndSend("/topic/online", new UserStatus(email, true));

//            System.out.println("--- Attempting to get online users list ---");
//            Set<String> onlineUsers = onlineUserService.getOnlineUsers();
//            System.out.println("--- Retrieved online users list. Size: " + (onlineUsers != null ? onlineUsers.size() : "null") + ". Content: " + onlineUsers + " ---");
//
//            if (onlineUsers != null) {
//                String userQueueDestination = "/queue/online-users";
//                System.out.println("--- Sending initial online users list to user: " + email + " on destination: " + userQueueDestination + " ---");
//                messagingTemplate.convertAndSendToUser(email, userQueueDestination, onlineUsers);
//                System.out.println("--- Initial online users list message sent ---");
//            } else {
//                System.err.println("--- Online users list was null, not sending message. ---");
//            }
        } else {
            System.out.println("--- WebSocket Session Connected, but user principal is null. ---");
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
