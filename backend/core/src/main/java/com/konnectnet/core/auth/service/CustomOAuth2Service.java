package com.konnectnet.core.auth.service;

import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.auth.enums.AuthProvider;
import com.konnectnet.core.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2Service extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found in OAuth2 provider response");
        }

        Optional<AppUser> optionalUser = userRepository.findByEmail(email);
        AppUser user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            // Optional: update provider if not set
            if (user.getProvider() == null) {
                user.setProvider(AuthProvider.GOOGLE);
                userRepository.save(user);
            }
        } else {
            // Create new user
            user = new AppUser();
            user.setEmail(email);
            user.setName(name);
            user.setProvider(AuthProvider.GOOGLE);
            userRepository.save(user);
        }

        return oAuth2User;
    }
}