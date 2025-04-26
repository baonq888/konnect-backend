package com.konnectnet.core.e2e;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konnectnet.core.e2e.utils.TokenContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthTest {

    private static final String BASE_URL = "http://localhost:8050/api/v1/auth";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    }

    private static final List<User> USERS = List.of(
            new User("User One", "user1@example.com", "123"),
            new User("User Two", "user2@example.com", "123"),
            new User("User Three", "user3@example.com", "123")
    );

    @Test
    @Order(1)
    void testGoogleLoginRedirect() {
        given()
                .baseUri("http://localhost:8050") // override only for this test
                .redirects().follow(false)
                .when()
                .get("/oauth2/authorization/google")
                .then()
                .statusCode(302)
                .header("Location", containsString("accounts.google.com"));
    }


    @Test
    @Order(2)
    void testRegisterUser() throws JsonProcessingException {

        for (User user : USERS) {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("name", user.name);
            requestMap.put("email", user.email);
            requestMap.put("password", user.password);

            String jsonBody = objectMapper.writeValueAsString(requestMap);

            var response =
                    given()
                            .contentType(ContentType.JSON)
                            .body(jsonBody)
                            .log().all()
                            .when()
                            .post("/register")
                            .then()
                            .log().all()
                            .extract()
                            .response();

            int statusCode = response.getStatusCode();

            Assertions.assertTrue(
                    statusCode == 201 || statusCode == 409,
                    "Unexpected response: " + statusCode + " - " + response.getBody().asPrettyString()
            );
        }
    }

    @Test
    @Order(3)
    void testLoginUser() throws JsonProcessingException {
        for (User user : USERS) {
            Map<String, String> loginPayload = new HashMap<>();
            loginPayload.put("email", user.email);
            loginPayload.put("password", user.password);

            String jsonBody = objectMapper.writeValueAsString(loginPayload);

            var response =
                    given()
                            .contentType(ContentType.JSON)
                            .body(jsonBody)
                            .when()
                            .post("/login")
                            .then()
                            .statusCode(200)
                            .body("access_token", notNullValue())
                            .body("refresh_token", notNullValue())
                            .extract()
                            .response();

            String accessToken = response.jsonPath().getString("access_token");
            String refreshToken = response.jsonPath().getString("refresh_token");

            TokenContext.add( user.email + "_" + "access_token", accessToken);
            TokenContext.add(user.email + "_" +"refresh_token", refreshToken);
        }
    }

    static class User {
        String name;
        String email;
        String password;

        public User(String name, String email, String password) {
            this.name = name;
            this.email = email;
            this.password = password;
        }
    }

    static class OAuthUser {
        String email;

        public OAuthUser(String email) {
            this.email = email;
        }
    }
}
