package com.konnectnet.core.friend.repository;

import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.friend.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {
    Optional<FriendRequest> findBySenderAndReceiver(AppUser sender, AppUser receiver);
}