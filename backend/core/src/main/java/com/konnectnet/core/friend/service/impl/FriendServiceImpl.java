package com.konnectnet.core.friend.service.impl;

import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.auth.repository.UserRepository;
import com.konnectnet.core.friend.entity.FriendRequest;
import com.konnectnet.core.friend.enums.FriendRequestStatus;
import com.konnectnet.core.friend.event.FriendRequestEvent;
import com.konnectnet.core.friend.exception.FriendException;
import com.konnectnet.core.friend.kafka.FriendNotificationProducer;
import com.konnectnet.core.friend.repository.FriendRequestRepository;
import com.konnectnet.core.friend.service.FriendService;
import com.konnectnet.core.notification.enums.NotificationType;
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
    private final FriendRequestRepository friendRequestRepository;
    private final UserMapper userMapper;
    private final FriendNotificationProducer friendNotificationProducer;

    @Override
    @Transactional
    public void sendFriendRequest(UUID senderId, UUID receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }

        AppUser sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("Sender not found"));
        AppUser receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new EntityNotFoundException("Receiver not found"));

        friendRequestRepository.findBySenderAndReceiver(sender, receiver).ifPresent(existing -> {
            throw new FriendException("Friend request already sent");
        });

        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(FriendRequestStatus.PENDING);

        FriendRequestEvent event = FriendRequestEvent.builder()
                .type(String.valueOf(NotificationType.FRIEND_REQUEST_SENT))
                .content(sender.getName() + " sent you a friend request")
                .senderId(senderId.toString())
                .senderName(sender.getName())
                .recipientId(receiverId.toString())
                .build();
        friendNotificationProducer.sendNotification(event);


        friendRequestRepository.save(request);
    }

    @Override
    @Transactional
    public void acceptFriendRequest(UUID receiverId, UUID senderId) {
        AppUser sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("Sender not found"));
        AppUser receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new EntityNotFoundException("Receiver not found"));

        FriendRequest request = friendRequestRepository.findBySenderAndReceiver(sender, receiver)
                .orElseThrow(() -> new FriendException("Friend request not found"));

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new FriendException("Friend request already handled");
        }

        request.setStatus(FriendRequestStatus.ACCEPTED);
        friendRequestRepository.save(request);

        // Add each other as friends
        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);

        // Follow each other
        sender.getFollowing().add(receiver);

        receiver.getFollowing().add(sender);


        FriendRequestEvent event = FriendRequestEvent.builder()
                .type(String.valueOf(NotificationType.FRIEND_REQUEST_ACCEPTED))
                .content(sender.getName() + " accepted your friend request")
                .senderId(receiverId.toString())
                .senderName(receiver.getName())
                .recipientId(senderId.toString())
                .build();
        friendNotificationProducer.sendNotification(event);

        userRepository.save(sender);
        userRepository.save(receiver);
    }

    @Override
    @Transactional
    public void unfriend(UUID userId, UUID friendId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        AppUser friend = userRepository.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("Friend not found"));

        // Remove from friends list
        user.getFriends().remove(friend);
        friend.getFriends().remove(user);

        // Remove follow relationships
        user.getFollowing().remove(friend);

        friend.getFollowing().remove(user);


        userRepository.save(user);
        userRepository.save(friend);
    }

    @Override
    public Page<UserDetailDTO> getFriends(UUID userId, Pageable pageable) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<AppUser> friends = new ArrayList<>(user.getFriends());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), friends.size());

        List<UserDetailDTO> friendDTOs = friends.subList(start, end).stream()
                .map(userMapper::toUserDetailDTO)
                .toList();

        return new PageImpl<>(friendDTOs, pageable, friends.size());
    }
}