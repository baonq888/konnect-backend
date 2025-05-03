package com.konnectnet.core.friend.service;

import com.konnectnet.core.user.dto.response.UserDetailDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface FriendService {
    void addFriend(UUID userId, UUID friendId);
    void unfriend(UUID userId, UUID friendId);
    Page<UserDetailDTO> getFriends(UUID userId, Pageable pageable);
}
