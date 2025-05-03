package com.konnectnet.core.follow.service.impl;

import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.auth.repository.UserRepository;
import com.konnectnet.core.follow.exception.FollowException;
import com.konnectnet.core.follow.service.FollowService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowServiceImpl implements FollowService {

    private final UserRepository userRepository;


    @Override
    @Transactional
    public void followUser(UUID followerId, UUID followeeId) {
        try {
            AppUser follower = userRepository.findById(followerId)
                    .orElseThrow(() -> new RuntimeException("Follower not found"));

            AppUser followee = userRepository.findById(followeeId)
                    .orElseThrow(() -> new RuntimeException("User to follow not found"));

            follower.getFollowing().add(followee);
            followee.getFollowers().add(follower);

            userRepository.save(follower);
            userRepository.save(followee);
        } catch (Exception e) {
            log.error("Error occurred while following user: {}", e.getMessage(), e);
            throw new FollowException("Failed to follow user", e);
        }

    }

    @Override
    @Transactional
    public void unfollowUser(UUID followerId, UUID followeeId) {
        try {
            AppUser follower = userRepository.findById(followerId)
                    .orElseThrow(() -> new RuntimeException("Follower not found"));

            AppUser followee = userRepository.findById(followeeId)
                    .orElseThrow(() -> new RuntimeException("User to unfollow not found"));

            follower.getFollowing().remove(followee);
            followee.getFollowers().remove(follower);

            userRepository.save(follower);
            userRepository.save(followee);
        } catch (Exception e) {
            log.error("Error occurred while unfollowing user: {}", e.getMessage(), e);
            throw new FollowException("Failed to unfollow user", e);
        }

    }

    @Override
    public List<AppUser> getFollowers(UUID userId) {
        try {
            AppUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return List.copyOf(user.getFollowers());
        } catch (Exception e) {
            log.error("Error occurred while getting followers: {}", e.getMessage(), e);
            throw new FollowException("Failed to get followers", e);
        }

    }

    @Override
    public List<AppUser> getFollowing(UUID userId) {
        try {
            AppUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return List.copyOf(user.getFollowing());
        } catch (Exception e) {
            log.error("Error occurred while getting followings: {}", e.getMessage(), e);
            throw new FollowException("Failed to get followings", e);        }

    }
}
