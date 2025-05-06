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
public class FollowTest {

    private static final String BASE_URL = "http://localhost:8050/api/v1/profile/follows";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TEST_USER_1 = "user1@example.com";
    private static final String TEST_USER_3 = "user3@example.com";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    private String getAccessToken(String user) {
        return TokenContext.get(user + "_access_token");
    }

    private String getUserId(String user) {
        return EntityContext.get(user + "_id");
    }

    @Test
    @Order(1)
    public void testFollowUser() {
        String token = getAccessToken(TEST_USER_1);
        String followerId = getUserId(TEST_USER_1);
        String followeeId = getUserId(TEST_USER_3);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .queryParam("followerId", followerId)
                .queryParam("followeeId", followeeId)
                .when()
                .post()
                .then()
                .statusCode(200)
                .body(equalTo("User followed successfully"));
    }

    @Test
    @Order(2)
    public void testGetFollowing() {
        String token = getAccessToken(TEST_USER_1);
        String userId = getUserId(TEST_USER_1);

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("userId", userId)
                .when()
                .get("/following")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(3)
    public void testGetFollowers() {
        String token = getAccessToken(TEST_USER_3);
        String userId = getUserId(TEST_USER_3);

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("userId", userId)
                .when()
                .get("/followers")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(4)
    public void testUnfollowUser() {
        String token = getAccessToken(TEST_USER_1);
        String followerId = getUserId(TEST_USER_1);
        String followeeId = getUserId(TEST_USER_3);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .queryParam("followerId", followerId)
                .queryParam("followeeId", followeeId)
                .when()
                .delete()
                .then()
                .statusCode(200)
                .body(equalTo("User unfollowed successfully"));
    }
}