package com.konnectnet.core.post.repository;

import com.konnectnet.core.post.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PhotoRepository extends JpaRepository<Photo, UUID> {
}
