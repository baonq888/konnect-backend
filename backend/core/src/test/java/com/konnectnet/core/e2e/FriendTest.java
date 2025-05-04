package com.konnectnet.core.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konnectnet.core.e2e.utils.TokenContext;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

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

}
