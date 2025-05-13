package com.konnectnet.core.post.repository;

import com.konnectnet.core.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    @Query("""
        SELECT p FROM Post p
        WHERE p.user.id IN (
            SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId
        )
        ORDER BY p.createdAt DESC
    """)
    Page<Post> findRecentPostsFromFollowedUsers(@Param("userId") UUID userId, Pageable pageable);
}