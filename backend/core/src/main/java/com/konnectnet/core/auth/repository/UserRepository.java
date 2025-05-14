package com.konnectnet.core.auth.repository;

import com.konnectnet.core.auth.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmail(String username);
    @Query("SELECT u FROM AppUser u JOIN u.following f WHERE f.id = :userId")
    List<AppUser> findFollowersByUserId(@Param("userId") UUID userId);
}
