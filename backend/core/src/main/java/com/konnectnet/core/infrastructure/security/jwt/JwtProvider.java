package com.konnectnet.core.infrastructure.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.auth.entity.Role;
import com.konnectnet.core.infrastructure.security.enums.JwtSecret;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    private final String secret = JwtSecret.SECRET_KEY.getKey();
    private final Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());


    public String generateAccessToken(AppUser appUser, HttpServletRequest request) {
        long expiry = 10 * 60 * 1000;
        return JWT.create()
                .withSubject(appUser.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiry))
                .withIssuer(request.getRequestURL().toString())
                .withClaim("user_id", appUser.getId().toString())
                .withClaim("roles", appUser.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .sign(algorithm);
    }

    public String generateRefreshToken(AppUser appUser, HttpServletRequest request) {
        long expiry = 30 * 60 * 1000;
        return JWT.create()
                .withSubject(appUser.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis()+expiry))
                .withIssuer(request.getRequestURL().toString())
                .sign(algorithm);
    }
}