package com.konnectnet.core.friend.service;

import com.konnectnet.core.user.dto.response.UserDetailDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface FriendService {
    void sendFriendRequest(UUID senderId, UUID receiverId);
    void acceptFriendRequest(UUID receiverId, UUID senderId);
    void unfriend(UUID userId, UUID friendId);
    Page<UserDetailDTO> getFriends(UUID userId, Pageable pageable);
}