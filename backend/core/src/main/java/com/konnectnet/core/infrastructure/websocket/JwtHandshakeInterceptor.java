package com.konnectnet.core.infrastructure.websocket;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.konnectnet.core.infrastructure.security.enums.JwtSecret;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import com.auth0.jwt.exceptions.TokenExpiredException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final Algorithm algorithm = Algorithm.HMAC256(JwtSecret.SECRET_KEY.getKey().getBytes());


    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {


        HttpHeaders headers = request.getHeaders();
        List<String> authorizationHeaders = headers.get(HttpHeaders.AUTHORIZATION);

        String token = null;
        if (authorizationHeaders != null && !authorizationHeaders.isEmpty()) {
            String authorizationHeader = authorizationHeaders.get(0);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring("Bearer ".length());
            }
        }


        if (token != null) {
            try {
                DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);
                String email = decodedJWT.getSubject();
                attributes.put("email", email);
                attributes.put("token", token);

            } catch (TokenExpiredException expiredException) {
                System.err.println("JWT validation failed: Token has expired. Expiration time: " + expiredException.getExpiredOn());
                return false;
            } catch (SignatureVerificationException signatureException) { // Also catch signature specific errors
                System.err.println("JWT validation failed: Invalid signature. " + signatureException.getMessage());
                return false;
            } catch (Exception e) {
                System.err.println("JWT validation failed during WebSocket handshake: Other error: " + e.getMessage());
                return false;
            }
            return true;
        } else {
            System.out.println("No Authorization Bearer token found...");
            attributes.remove("email");
            return true;
        }

    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {}


}
