package com.konnectnet.core.infrastructure.websocket;

import com.konnectnet.core.infrastructure.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    private final JwtUtil jwtUtil;

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        String token = (String) attributes.get("token");
        String email = (String) attributes.get("email");

        if (email != null) {

            List<GrantedAuthority> authorities = getUserAuthorities(token);


            return new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    authorities
            );
        }


        return null;
    }

    private List<GrantedAuthority> getUserAuthorities(String token) {
        try {
            List<String> roles = jwtUtil.extractRoles(token);

            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error extracting roles or creating authorities from token: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
