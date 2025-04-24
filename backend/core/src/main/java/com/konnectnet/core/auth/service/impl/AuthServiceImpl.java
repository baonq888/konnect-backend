package com.konnectnet.core.auth.service.impl;

import com.konnectnet.core.auth.dto.request.RegisterRequest;
import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.auth.entity.Role;
import com.konnectnet.core.auth.repository.RoleRepository;
import com.konnectnet.core.auth.repository.UserRepository;
import com.konnectnet.core.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService, UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<AppUser> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            log.error("user not found");
            throw new UsernameNotFoundException("user not found");
        }
        AppUser user = userOptional.get();
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        });
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }

    @Override
    public AppUser saveUser(RegisterRequest request) {
        AppUser user = new AppUser(request.getName(), request.getEmail(), request.getPassword());

        Role userRole = roleRepository.findByName(com.konnectnet.core.auth.enums.Role.USER.name())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of(userRole));

        return userRepository.save(user);
    }

    @Override
    public AppUser getUser(String email) {
        Optional<AppUser> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            log.error("User not found");
            throw new UsernameNotFoundException("user not found");
        }
        return userOptional.get();
    }


}
