package com.konnectnet.core.friend.service.impl;

import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.auth.repository.UserRepository;
import com.konnectnet.core.friend.exception.FriendException;
import com.konnectnet.core.friend.service.FriendService;
import com.konnectnet.core.user.dto.response.UserDetailDTO;
import com.konnectnet.core.user.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendServiceImpl implements FriendService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void addFriend(UUID userId, UUID friendId) {
        try {
            if (userId.equals(friendId)) {
                throw new IllegalArgumentException("User cannot add themselves as a friend");
            }

            AppUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            AppUser friend = userRepository.findById(friendId)
                    .orElseThrow(() -> new IllegalArgumentException("Friend not found"));

            user.getFriends().add(friend);
            friend.getFriends().add(user);

            userRepository.save(user);
            userRepository.save(friend);
        } catch (Exception e) {
            log.error("Error occurred while adding friend: {}", e.getMessage(), e);
            throw new FriendException("Failed to add friend", e);
        }

    }

    @Override
    @Transactional
    public void unfriend(UUID userId, UUID friendId) {
        try {
            AppUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            AppUser friend = userRepository.findById(friendId)
                    .orElseThrow(() -> new IllegalArgumentException("Friend not found"));

            user.getFriends().remove(friend);
            friend.getFriends().remove(user);

            userRepository.save(user);
            userRepository.save(friend);
        } catch (Exception e) {
            log.error("Error occurred while removing friend: {}", e.getMessage(), e);
            throw new FriendException("Failed to remove friend", e);
        }

    }

    @Override
    public Page<UserDetailDTO> getFriends(UUID userId, Pageable pageable) {
        try {
            AppUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            List<AppUser> friends = new ArrayList<>(user.getFriends());

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), friends.size());

            List<UserDetailDTO> friendDTOs = friends.subList(start, end).stream()
                    .map(userMapper::toUserDetailDTO)
                    .toList();

            return new PageImpl<>(friendDTOs, pageable, friends.size());
        } catch (Exception e) {
            log.error("Error occurred while getting friends: {}", e.getMessage(), e);
            throw new FriendException("Failed to get friends", e);
        }
    }
}