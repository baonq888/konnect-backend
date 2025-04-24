package com.konnectnet.core.infrastructure.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.auth.entity.Role;
import com.konnectnet.core.infrastructure.security.enums.JwtSecret;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    private final String secret = JwtSecret.SECRET_KEY.getKey();
    Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());

    public String generateToken(User user, HttpServletRequest request) {

        long expiry = 10 * 60 * 1000;
        return JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiry))
                .withIssuer(request.getRequestURL().toString())
                .withClaim("roles",user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(algorithm);
    }

    public String generateToken(AppUser appUser, HttpServletRequest request) {
        long expiry = 10 * 60 * 1000;
        return JWT.create()
                .withSubject(appUser.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiry))
                .withIssuer(request.getRequestURL().toString())
                .withClaim("roles", appUser.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .sign(algorithm);
    }
}