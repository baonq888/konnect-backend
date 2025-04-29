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
    private static final String TEST_USER = "user1@example.com";


    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
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
        postRequest.put("photoUrls", List.of());

        String jsonBody = objectMapper.writeValueAsString(postRequest);

        var response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + getAccessToken())
                .body(jsonBody)
                .when()
                .post()
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("content", equalTo("This is a test post"))
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
                .get()
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
    @Order(5)
    void testDeletePost() {
        Assumptions.assumeTrue(postId != null);

        given()
                .header("Authorization", "Bearer " + getAccessToken())
                .when()
                .delete("/{postId}", postId)
                .then()
                .statusCode(200)
                .body(equalTo("Post deleted successfully"));
    }
}
