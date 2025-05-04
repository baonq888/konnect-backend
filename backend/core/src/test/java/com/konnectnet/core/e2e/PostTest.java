package com.konnectnet.core.e2e;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konnectnet.core.e2e.utils.TokenContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostTest {

    private static final String BASE_URL = "http://localhost:8050/api/v1/posts";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static String postId;
    private static String sharedPostId;
    private static String commentId;
    private static final String TEST_USER = "user1@example.com";


    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    private String getAccessToken() {
        return TokenContext.get(TEST_USER + "_" + "access_token");
    }

    @Test
    @Order(1)
    void testCreatePost() throws JsonProcessingException {
        Map<String, Object> postRequest = new HashMap<>();
        postRequest.put("content", "This is a test post");
        postRequest.put("visibility", "PUBLIC");
        postRequest.put("photoUrls", List.of("https://images.unsplash.com/photo-1745794621090-d856c53b0cc2?q=80&w=2940&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"));

        String jsonBody = objectMapper.writeValueAsString(postRequest);
        System.out.println(jsonBody);


        var response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + getAccessToken())
                .body(jsonBody)
                .log().all()
                .when()
                .post("")
                .then()
                .statusCode(201)
                .extract()
                .response();

        postId = response.jsonPath().getString("id");
    }

    @Test
    @Order(2)
    void testGetPostById() {
        Assumptions.assumeTrue(postId != null);

        given()
                .header("Authorization", "Bearer " + getAccessToken())
                .when()
                .get("/{postId}", postId)
                .then()
                .statusCode(200)
                .body("id", equalTo(postId));
    }

    @Test
    @Order(3)
    void testSearchPosts() {
        given()
                .header("Authorization", "Bearer " + getAccessToken())
                .queryParam("searchTerm", "test")
                .queryParam("page", 0)
                .queryParam("limit", 10)
                .when()
                .get("/")
                .then()
                .statusCode(200)
                .body("content", not(empty()));
    }

    @Test
    @Order(4)
    void testUpdatePost() throws JsonProcessingException {
        Assumptions.assumeTrue(postId != null);

        Map<String, Object> updatedPost = new HashMap<>();
        updatedPost.put("content", "Updated content for the test post");
        updatedPost.put("visibility", "PRIVATE");
        updatedPost.put("photoUrls", List.of());

        String jsonBody = objectMapper.writeValueAsString(updatedPost);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + getAccessToken())
                .body(jsonBody)
                .when()
                .put("/{postId}", postId)
                .then()
                .statusCode(200)
                .body("content", equalTo("Updated content for the test post"))
                .body("visibility", equalTo("PRIVATE"));
    }


    @Test
    @Order(6)
    void testLikePost() {
        Assumptions.assumeTrue(postId != null);
        given()
                .header("Authorization", "Bearer " + getAccessToken())
                .queryParam("userId", TEST_USER)
                .when()
                .post("/{postId}/like", postId)
                .then()
                .statusCode(200)
                .body(equalTo("Post liked successfully"));
    }

    @Test
    @Order(7)
    void testUnlikePost() {
        Assumptions.assumeTrue(postId != null);
        given()
                .header("Authorization", "Bearer " + getAccessToken())
                .queryParam("userId", TEST_USER)
                .when()
                .post("/{postId}/unlike", postId)
                .then()
                .statusCode(200)
                .body(equalTo("Post unliked successfully"));
    }

    @Test
    @Order(8)
    void testSharePost() {
        Assumptions.assumeTrue(postId != null);
        given()
                .header("Authorization", "Bearer " + getAccessToken())
                .queryParam("userId", TEST_USER)
                .body("Shared this post!")
                .contentType(ContentType.TEXT)
                .when()
                .post("/{postId}/share", postId)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("content", containsString("Shared"))
                .extract().response();

        sharedPostId = given()
                .header("Authorization", "Bearer " + getAccessToken())
                .get("/{postId}", postId)
                .then()
                .extract().path("id"); // Capture shared post ID from response if needed
    }

    @Test
    @Order(9)
    void testUnsharePost() {
        Assumptions.assumeTrue(sharedPostId != null);
        given()
                .header("Authorization", "Bearer " + getAccessToken())
                .when()
                .delete("/{postId}/unshare", sharedPostId)
                .then()
                .statusCode(200)
                .body(equalTo("Shared post removed successfully"));
    }

    @Test
    @Order(10)
    void testCommentOnPost() {
        Assumptions.assumeTrue(postId != null);
        var response = given()
                .header("Authorization", "Bearer " + getAccessToken())
                .queryParam("userId", TEST_USER)
                .body("Nice post!")
                .contentType(ContentType.TEXT)
                .when()
                .post("/{postId}/comments", postId)
                .then()
                .statusCode(201)
                .body("text", equalTo("Nice post!"))
                .extract().response();

        commentId = response.jsonPath().getString("id");
    }

    @Test
    @Order(11)
    void testLikeComment() {
        Assumptions.assumeTrue(commentId != null);
        given()
                .header("Authorization", "Bearer " + getAccessToken())
                .queryParam("userId", TEST_USER)
                .when()
                .post("/{postId}/comments/{commentId}/like", postId, commentId)
                .then()
                .statusCode(200)
                .body(equalTo("Comment liked successfully"));
    }

    @Test
    @Order(12)
    void testUnlikeComment() {
        Assumptions.assumeTrue(commentId != null);
        given()
                .header("Authorization", "Bearer " + getAccessToken())
                .queryParam("userId", TEST_USER)
                .when()
                .post("/{postId}/comments/{commentId}/unlike", postId, commentId)
                .then()
                .statusCode(200)
                .body(equalTo("Comment unliked successfully"));
    }
}
