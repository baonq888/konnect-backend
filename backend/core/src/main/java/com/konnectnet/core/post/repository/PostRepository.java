package com.konnectnet.core.post.repository;

import com.konnectnet.core.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    Page<Post> findAllById(Iterable<UUID> postIds, Pageable pageable);
}
