package com.konnectnet.core.auth.service.impl;

import com.konnectnet.core.auth.dto.request.RegisterRequest;
import com.konnectnet.core.auth.dto.response.AppUserDTO;
import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.auth.entity.Role;
import com.konnectnet.core.auth.enums.RoleEnum;
import com.konnectnet.core.auth.mapper.AppUserMapper;
import com.konnectnet.core.auth.repository.RoleRepository;
import com.konnectnet.core.auth.repository.UserRepository;
import com.konnectnet.core.auth.service.AuthService;
import com.konnectnet.core.infrastructure.security.AppUserDetails;
import com.konnectnet.core.user.entity.UserDetail;
import jakarta.transaction.Transactional;
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
    private final AppUserMapper appUserMapper;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found");
                });

        return new AppUserDetails(user);
    }

    @Override
    @Transactional
    public AppUserDTO saveUser(RegisterRequest request) {
        AppUser user = new AppUser(request.getName(), request.getEmail(), request.getPassword());
        Role userRole = roleRepository.findByName(RoleEnum.USER.name())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of(userRole));

        UserDetail userDetail = new UserDetail();
        userDetail.setUser(user);
        user.setUserDetail(userDetail);

        AppUser savedUser = userRepository.save(user);
        return appUserMapper.toDto(savedUser);
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

    @Override
    public void saveRole(String roleName) {
        Optional<Role> userRole = roleRepository.findByName(roleName);
        if (userRole.isEmpty()) {
            roleRepository.save(new Role(roleName));
        }
    }


}
