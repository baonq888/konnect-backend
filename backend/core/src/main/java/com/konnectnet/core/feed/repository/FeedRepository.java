package com.konnectnet.core.feed.repository;

import com.konnectnet.core.feed.entity.Feed;
import com.konnectnet.core.auth.entity.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FeedRepository extends JpaRepository<Feed, UUID> {
    Page<Feed> findByUserOrderByAddedAtDesc(AppUser user, Pageable pageable);
}