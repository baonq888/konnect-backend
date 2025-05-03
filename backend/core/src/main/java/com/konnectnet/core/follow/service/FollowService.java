package com.konnectnet.core.follow.service;

import com.konnectnet.core.auth.entity.AppUser;

import java.util.List;
import java.util.UUID;

public interface FollowService {

    void followUser(UUID followerId, UUID followeeId);

    void unfollowUser(UUID followerId, UUID followeeId);

    List<AppUser> getFollowers(UUID userId);

    List<AppUser> getFollowing(UUID userId);
}