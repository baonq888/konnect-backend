package com.konnectnet.core.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konnectnet.core.e2e.utils.EntityContext;
import com.konnectnet.core.e2e.utils.TokenContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FriendTest {

    private static final Logger logger = LoggerFactory.getLogger(FriendTest.class); // Added logger

    private static final String BASE_URL = "http://localhost:8050";
    private static final String WS_URL = "ws://localhost:8050/ws";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TEST_USER_1 = "user1@example.com";
    private static final String TEST_USER_2 = "user2@example.com";

    private BlockingQueue<String> messageQueueUser1;
    private BlockingQueue<String> messageQueueUser2;

    private StompSession stompSessionUser1;
    private StompSession stompSessionUser2;

    @BeforeAll
    static void globalSetup() {
        // Simulate token and entity population if not done elsewhere
        // This is crucial. If TokenContext or EntityContext are empty, tests will fail.
        // Example:
        // TokenContext.put("user1@example.com_access_token", "dummy_token_user1");
        // TokenContext.put("user2@example.com_access_token", "dummy_token_user2");
        // EntityContext.put("user1@example.com_id", "user1-id");
        // EntityContext.put("user2@example.com_id", "user2-id");
        logger.info("Global setup: Ensure TokenContext and EntityContext are populated.");
    }

    @BeforeEach
    void setup() throws Exception {
        RestAssured.baseURI = BASE_URL;

        messageQueueUser1 = new LinkedBlockingQueue<>();
        messageQueueUser2 = new LinkedBlockingQueue<>();

        logger.info("Setting up WebSocket for {}", TEST_USER_1);
        stompSessionUser1 = connectAndSubscribe(TEST_USER_1, messageQueueUser1);
        logger.info("Setting up WebSocket for {}", TEST_USER_2);
        stompSessionUser2 = connectAndSubscribe(TEST_USER_2, messageQueueUser2);
    }

    @AfterEach
    void tearDown() {
        logger.info("Tearing down WebSocket connections.");
        if (stompSessionUser1 != null && stompSessionUser1.isConnected()) {
            logger.info("Disconnecting STOMP session for {}", TEST_USER_1);
            stompSessionUser1.disconnect();
        } else if (stompSessionUser1 != null) {
            logger.warn("STOMP session for {} was not connected before teardown.", TEST_USER_1);
        }

        if (stompSessionUser2 != null && stompSessionUser2.isConnected()) {
            logger.info("Disconnecting STOMP session for {}", TEST_USER_2);
            stompSessionUser2.disconnect();
        } else if (stompSessionUser2 != null) {
            logger.warn("STOMP session for {} was not connected before teardown.", TEST_USER_2);
        }
    }

    private StompSession connectAndSubscribe(String userEmail, BlockingQueue<String> queue) throws Exception {
        logger.info("Attempting to connect WebSocket for user: {}", userEmail);

        String token = getAccessToken(userEmail);
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        StompHeaders connectHeaders = new StompHeaders();

        connectHeaders.add("username", userEmail);

        logger.info("Connecting with token: Bearer {}...", token.substring(0, Math.min(token.length(), 10))); // Log only a part of the token

        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                logger.info("STOMP session connected for {}. Session ID: {}. ConnectedHeaders: {}", userEmail, session.getSessionId(), connectedHeaders);
                // Subscribe after connection is confirmed
                session.subscribe("/user/queue/notifications", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return String.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        logger.info("Received STOMP message for {}: {}. Headers: {}", userEmail, payload, headers);
                        queue.offer((String) payload);
                    }
                });
                logger.info("Subscribed to /user/queue/notifications for {}", userEmail);
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                logger.error("STOMP Exception for {}. Command: {}, Headers: {}, Payload: {}, Exception: {}",
                        userEmail, command, headers, new String(payload), exception.getMessage(), exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                logger.error("STOMP Transport Error for {}. Session ID (if available): {}. Exception: {}",
                        userEmail, session != null ? session.getSessionId() : "N/A", exception.getMessage(), exception);
                // This is often where ConnectionLostException details will appear
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                // This is for unexpected frames not handled by a specific subscription.
                logger.warn("Received unexpected STOMP frame for {}. Headers: {}. Payload: {}", userEmail, headers, payload);
            }
        };

        int attempts = 0;
        StompSession session = null;
        Exception lastException = null;
        final int MAX_ATTEMPTS = 2;
        final long RETRY_DELAY_MS = 1000; // Increased delay

        while (attempts < MAX_ATTEMPTS) {
            try {
                logger.info("Attempt {} to connect WebSocket for {}", attempts + 1, userEmail);
                // Use WebSocketHttpHeaders for WebSocket specific headers, connectHeaders for STOMP CONNECT frame
                session = stompClient
                        .connectAsync(WS_URL, headers, connectHeaders, sessionHandler)
                        .get(1, TimeUnit.SECONDS);

                if (session != null && session.isConnected()) {
                    logger.info("Successfully connected WebSocket for {} on attempt {}", userEmail, attempts + 1);
                    break; // Exit loop on successful connection
                } else {
                    logger.warn("Connection attempt {} for {} resulted in a null or disconnected session.", attempts + 1, userEmail);
                    // This case should ideally be caught by an exception from .get() or handled by isConnected()
                }

            } catch (Exception ex) {
                lastException = ex;
                logger.error("Generic exception during connection attempt {} for {}: {}", attempts + 1, userEmail, ex.getMessage(), ex);
            }

            attempts++;
            if (attempts < MAX_ATTEMPTS) {
                logger.info("Retrying connection for {} in {} ms...", userEmail, RETRY_DELAY_MS);
                Thread.sleep(RETRY_DELAY_MS);
            }
        }

        if (session == null || !session.isConnected()) {
            logger.error("Failed to connect WebSocket for user {} after {} attempts.", userEmail, MAX_ATTEMPTS, lastException);
            throw new RuntimeException("Failed to connect WebSocket for user " + userEmail + " after " + MAX_ATTEMPTS + " attempts.", lastException);
        }


        return session;
    }

    private String getAccessToken(String user) {
        String token = TokenContext.get(user + "_access_token");
        if (token == null) {
            logger.error("getAccessToken: Token not found for user {}", user);
            // Consider throwing an exception here or ensure TokenContext is always populated
        }
        return token;
    }

    private String getUserId(String user) {
        String userId = EntityContext.get(user + "_id");
        if (userId == null) {
            logger.error("getUserId: User ID not found for user {}", user);
            // Consider throwing an exception here or ensure EntityContext is always populated
        }
        return userId;
    }

    @Test
    @Order(1)
    void sendFriendRequest() throws Exception {
        logger.info("Executing test: sendFriendRequest");
        // Ensure sessions are connected before proceeding
        assertTrue(stompSessionUser1 != null && stompSessionUser1.isConnected(), "User 1 STOMP session is not connected for sendFriendRequest");
        assertTrue(stompSessionUser2 != null && stompSessionUser2.isConnected(), "User 2 STOMP session is not connected for sendFriendRequest");

        given()
                .header("Authorization", "Bearer " + getAccessToken(TEST_USER_1))
                .contentType(ContentType.JSON)
                .queryParam("senderId", getUserId(TEST_USER_1))
                .queryParam("receiverId", getUserId(TEST_USER_2))
                .when()
                .post("/api/v1/profile/friends/request")
                .then()
                .statusCode(200)
                .body(equalTo("Friend request sent successfully"));

        String message = messageQueueUser2.poll(10, TimeUnit.SECONDS); // Increased timeout for message polling
        assertNotNull(message, "WebSocket notification not received by User 2 for friend request");
        assertTrue(message.contains("sent you a friend request"), "Unexpected message for User 2: " + message);
        logger.info("sendFriendRequest: User 2 received notification: {}", message);
    }

    @Test
    @Order(2)
    void acceptFriendRequest() throws Exception {
        logger.info("Executing test: acceptFriendRequest");
        // Ensure sessions are connected
        assertTrue(stompSessionUser1 != null && stompSessionUser1.isConnected(), "User 1 STOMP session is not connected for acceptFriendRequest");
        assertTrue(stompSessionUser2 != null && stompSessionUser2.isConnected(), "User 2 STOMP session is not connected for acceptFriendRequest");

        given()
                .header("Authorization", "Bearer " + getAccessToken(TEST_USER_2))
                .contentType(ContentType.JSON)
                .queryParam("receiverId", getUserId(TEST_USER_2))
                .queryParam("senderId", getUserId(TEST_USER_1))
                .when()
                .post("/api/v1/profile/friends/accept")
                .then()
                .statusCode(200)
                .body(equalTo("Friend request accepted successfully"));

        String message = messageQueueUser1.poll(10, TimeUnit.SECONDS); // Increased timeout
        assertNotNull(message, "WebSocket notification not received by User 1 for friend request acceptance");
        assertTrue(message.contains("accepted your friend request"), "Unexpected message for User 1: " + message);
        logger.info("acceptFriendRequest: User 1 received notification: {}", message);
    }

    @Test
    @Order(3)
    void getFriendsForUser1() {
        logger.info("Executing test: getFriendsForUser1");
        given()
                .header("Authorization", "Bearer " + getAccessToken(TEST_USER_1))
                .queryParam("userId", getUserId(TEST_USER_1))
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/profile/friends")
                .then()
                .statusCode(200)
                .body("content.size()", greaterThan(0)) // Assumes user1 now has user2 as a friend
                .body("content[0].id", notNullValue());
        logger.info("getFriendsForUser1: Successfully retrieved friends for User 1.");
    }

    @Test
    @Order(4)
    void unfriend() {
        logger.info("Executing test: unfriend");
        given()
                .header("Authorization", "Bearer " + getAccessToken(TEST_USER_1))
                .queryParam("userId", getUserId(TEST_USER_1))
                .queryParam("friendId", getUserId(TEST_USER_2))
                .when()
                .delete("/api/v1/profile/friends")
                .then()
                .statusCode(200)
                .body(equalTo("Friend removed successfully"));
        logger.info("unfriend: Successfully unfriended User 2 from User 1's perspective.");
    }
}
