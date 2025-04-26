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

            List<GrantedAuthority> authorities = getUserAuthorities(email, token);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    authorities
            );

            authentication.setAuthenticated(true);

            return authentication;
        }


        return null;
    }

    private List<GrantedAuthority> getUserAuthorities(String email, String token) {
        Claims claims = jwtUtil.extractAllClaims(token);
        String username = claims.getSubject();
        List<String> roles = jwtUtil.extractRoles(token);

        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        return Collections.emptyList();
    }
}
