package com.konnectnet.core.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konnectnet.core.e2e.utils.EntityContext;
import com.konnectnet.core.e2e.utils.TokenContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
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
    private static final String BASE_URL = "http://localhost:8050/api/v1/profile/friends";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TEST_USER_1 = "user1@example.com";
    private static final String TEST_USER_2 = "user2@example.com";

    private static BlockingQueue<String> messageQueueUser1;
    private static BlockingQueue<String> messageQueueUser2;

    @BeforeAll
    static void setup() throws Exception {
        RestAssured.baseURI = BASE_URL;

        messageQueueUser1 = new LinkedBlockingQueue<>();
        messageQueueUser2 = new LinkedBlockingQueue<>();

        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession stompSessionUser1 = connectAndSubscribe(stompClient, TEST_USER_1, messageQueueUser1);
        StompSession stompSessionUser2 = connectAndSubscribe(stompClient, TEST_USER_2, messageQueueUser2);

        Thread.sleep(500);
    }

    private static StompSession connectAndSubscribe(WebSocketStompClient stompClient, String userEmail, BlockingQueue<String> queue) throws Exception {
        StompHeaders headers = new StompHeaders();
        headers.add("Authorization", "Bearer " + TokenContext.get(userEmail + "_access_token"));

        StompSession session = stompClient
                .connectAsync("ws://localhost:8050/ws", new WebSocketHttpHeaders(), headers, new StompSessionHandlerAdapter() {})
                .get(3, TimeUnit.SECONDS);

        session.subscribe("/user/queue/notifications", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue.offer((String) payload);
            }
        });

        return session;
    }

    private String getAccessToken(String user) {
        return TokenContext.get(user + "_access_token");
    }

    private String getUserId(String user) {
        return EntityContext.get(user + "_id");
    }

    @Test
    @Order(1)
    void sendFriendRequest() throws Exception {
        given()
                .header("Authorization", "Bearer " + getAccessToken(TEST_USER_1))
                .contentType(ContentType.JSON)
                .queryParam("senderId", getUserId(TEST_USER_1))
                .queryParam("receiverId", getUserId(TEST_USER_2))
                .when()
                .post("/request")
                .then()
                .statusCode(200)
                .body(equalTo("Friend request sent successfully"));

        String message = messageQueueUser2.poll(5, TimeUnit.SECONDS);
        assertNotNull(message, "WebSocket notification not received by User 2");
        assertTrue(message.contains("sent you a friend request"), "Unexpected message for User 2: " + message);
    }

    @Test
    @Order(2)
    void acceptFriendRequest() throws Exception {
        given()
                .header("Authorization", "Bearer " + getAccessToken(TEST_USER_2))
                .contentType(ContentType.JSON)
                .queryParam("receiverId", getUserId(TEST_USER_2))
                .queryParam("senderId", getUserId(TEST_USER_1))
                .when()
                .post("/accept")
                .then()
                .statusCode(200)
                .body(equalTo("Friend request accepted successfully"));

        String message = messageQueueUser1.poll(5, TimeUnit.SECONDS);
        assertNotNull(message, "WebSocket notification not received by User 1");
        assertTrue(message.contains("accepted your friend request"), "Unexpected message for User 1: " + message);
    }

    @Test
    @Order(3)
    void getFriendsForUser1() {
        given()
                .header("Authorization", "Bearer " + getAccessToken(TEST_USER_1))
                .queryParam("userId", getUserId(TEST_USER_1))
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("content.size()", greaterThan(0))
                .body("content[0].id", notNullValue());
    }

    @Test
    @Order(4)
    void unfriend() {
        given()
                .header("Authorization", "Bearer " + getAccessToken(TEST_USER_1))
                .queryParam("userId", getUserId(TEST_USER_1))
                .queryParam("friendId", getUserId(TEST_USER_2))
                .when()
                .delete()
                .then()
                .statusCode(200)
                .body(equalTo("Friend removed successfully"));
    }
}
