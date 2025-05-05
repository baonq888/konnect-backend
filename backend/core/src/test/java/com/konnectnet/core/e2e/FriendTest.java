package com.konnectnet.core.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konnectnet.core.e2e.utils.EntityContext;
import com.konnectnet.core.e2e.utils.TokenContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FriendTest {
    private static final String BASE_URL = "http://localhost:8050/api/v1/profile/friends";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TEST_USER_1 = "user1@example.com";
    private static final String TEST_USER_2 = "user2@example.com";

    @BeforeAll
    static void setup() { RestAssured.baseURI = BASE_URL; }

    private String getAccessToken(String user) {
        return TokenContext.get(user + "_" + "access_token");
    }
    private String getUserId(String user) { return EntityContext.get(user + "_id");}

    @Test
    @Order(1)
    void sendFriendRequest() {
        given()
                .header("Authorization", "Bearer " + getAccessToken("user1@example.com"))
                .contentType(ContentType.JSON)
                .queryParam("senderId", getUserId(TEST_USER_1))
                .queryParam("receiverId", getUserId(TEST_USER_2))
                .when()
                .post("/request")
                .then()
                .statusCode(200)
                .body(equalTo("Friend request sent successfully"));
    }

    @Test
    @Order(2)
    void acceptFriendRequest() {
        given()
                .header("Authorization", "Bearer " + getAccessToken("user2@example.com"))
                .contentType(ContentType.JSON)
                .queryParam("receiverId", getUserId(TEST_USER_2))
                .queryParam("senderId", getUserId(TEST_USER_1))
                .when()
                .post("/accept")
                .then()
                .statusCode(200)
                .body(equalTo("Friend request accepted successfully"));
    }

    @Test
    @Order(3)
    void getFriendsForUser1() {
        given()
                .header("Authorization", "Bearer " + getAccessToken("user1@example.com"))
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
                .header("Authorization", "Bearer " + getAccessToken("user1@example.com"))
                .queryParam("userId", getUserId(TEST_USER_1))
                .queryParam("friendId", getUserId(TEST_USER_2))
                .when()
                .delete()
                .then()
                .statusCode(200)
                .body(equalTo("Friend removed successfully"));
    }
}
