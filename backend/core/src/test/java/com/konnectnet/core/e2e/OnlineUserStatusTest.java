package com.konnectnet.core.e2e;

import com.konnectnet.core.e2e.utils.TokenContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class OnlineUserStatusTest {

    private String WS_URL;

    @BeforeEach
    void init() {
        WS_URL = "ws://localhost:8050/ws";
    }

    private static final String TEST_USER = "user1@example.com";

    @Test
    void testOnlineUserBroadcastAfterLogin() throws Exception {
        String accessToken = TokenContext.get(TEST_USER + "_" + "access_token");

        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
//        List<Transport> transports = new ArrayList<>();
//        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
//        WebSocketClient transport = new SockJsClient(transports);
//        WebSocketStompClient stompClient = new WebSocketStompClient(transport);

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("username", TEST_USER);

        CompletableFuture<Set<String>> onlineUsersFuture = new CompletableFuture<>();

        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(
                    @NonNull StompSession session, @NonNull StompHeaders connectedHeaders) {
                System.out.println("Successfully connected to WebSocket. Session ID: " + session.getSessionId());
                session.subscribe("/user/queue/online-users", new StompFrameHandler() {
                    @NonNull
                    @Override
                    public Type getPayloadType(@NonNull StompHeaders headers) {
                        return Set.class;
                    }

                    @Override
                    public void handleFrame(
                            @NonNull StompHeaders headers, @Nullable Object payload) {
                        System.out.println("Received frame on /user/queue/online-users: " + payload);
                        if (payload instanceof Set) {
                            try {
                                @SuppressWarnings("unchecked")
                                Set<String> users = (Set<String>) payload;
                                onlineUsersFuture.complete(users);
                            } catch (ClassCastException e) {
                                System.err.println("Payload type mismatch: Expected Set<String>, but got different Set type.");
                                onlineUsersFuture.completeExceptionally(new RuntimeException("Payload type mismatch", e));
                            }
                        } else if (payload != null) {
                            System.err.println("Received unexpected payload type: " + payload.getClass().getName());
                            onlineUsersFuture.completeExceptionally(new RuntimeException("Received unexpected payload type: " + payload.getClass().getName()));
                        } else {
                            System.err.println("Received null payload.");
                            onlineUsersFuture.complete(Collections.emptySet());
                        }
                    }
                });
            }

            @Override
            public void handleException(@NonNull StompSession session,
                                        @Nullable StompCommand command,
                                        @NonNull StompHeaders headers,
                                        @Nullable byte[] payload, @NonNull Throwable exception) { // Add @NonNull, @Nullable, @NonNull
                String payloadStr = (payload != null) ? new String(payload) : "[null]";
                if (!onlineUsersFuture.isDone()){
                    onlineUsersFuture.completeExceptionally(exception);
                }
            }


            @Override
            public void handleTransportError(
                    @NonNull StompSession session,
                    @NonNull Throwable exception) {
                if (!onlineUsersFuture.isDone()){
                    onlineUsersFuture.completeExceptionally(exception);
                }
            }
        };

        System.out.println("Attempting to connect to: " + WS_URL);
        var connectionFuture = stompClient.connectAsync(WS_URL, headers, connectHeaders, sessionHandler);

        StompSession session = null;
        try {
            session = connectionFuture.get(10, TimeUnit.SECONDS);
            System.out.println("Connection future completed. Session acquired.");
        } catch (TimeoutException ex) {
            System.err.println("Connection timed out after 10 seconds.");
            throw ex;
        } catch (ExecutionException ex) {
            System.err.println("Connection failed: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()));
            // Log the cause as well if available
            if (ex.getCause() != null) {
                ex.getCause().printStackTrace(System.err);
            }
            throw ex;
        }


        System.out.println("Waiting for online users list...");
        Set<String> onlineUsers = onlineUsersFuture.get(5, TimeUnit.SECONDS);

        System.out.println("Received online users: " + onlineUsers);

        assertNotNull(onlineUsers, "Online users set should not be null");
        assertTrue(onlineUsers.contains(TEST_USER), "Expected user '" + TEST_USER + "' not found in online list: " + onlineUsers);

        System.out.println("Disconnecting session...");
        if (session != null && session.isConnected()) {
            session.disconnect();
            System.out.println("Session disconnected.");
        } else {
            System.out.println("Session was null or not connected, skipping disconnect.");
        }
    }
}